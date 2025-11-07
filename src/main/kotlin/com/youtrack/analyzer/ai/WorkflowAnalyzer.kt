package com.youtrack.analyzer.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import com.youtrack.analyzer.api.dto.AnalysisResponse
import com.youtrack.analyzer.api.dto.WorkflowRuleInfo
import com.youtrack.analyzer.domain.model.Issue
import com.youtrack.analyzer.domain.model.WorkflowRule
import com.youtrack.analyzer.domain.repository.IssueRepository
import com.youtrack.analyzer.domain.repository.WorkflowRepository
import com.youtrack.analyzer.infrastructure.config.AIConfig
import com.youtrack.analyzer.infrastructure.config.YouTrackConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging

private val logger = KotlinLogging.logger("WorkflowAnalyzer")

class WorkflowAnalyzer(
    private val config: AIConfig,
    private val issueRepository: IssueRepository,
    private val workflowRepository: WorkflowRepository,
    private val youtrackConfig: YouTrackConfig
) {
    private val promptExecutor = simpleOpenAIExecutor(config.apiKey)

    init {
        logger.info { "WorkflowAnalyzer initialized with model ${config.model}" }
    }

    private var testAgent: AIAgent<String, String>? = null

    internal constructor(
        testAgent: AIAgent<String, String>,
        config: AIConfig,
        issueRepository: IssueRepository,
        workflowRepository: WorkflowRepository,
        youtrackConfig: YouTrackConfig
    ) : this(config, issueRepository, workflowRepository, youtrackConfig) {
        this.testAgent = testAgent
    }

    private fun createAgent(): AIAgent<String, String> {
        return AIAgent(
            promptExecutor = promptExecutor,
            llmModel = OpenAIModels.Chat.GPT4o,
            systemPrompt = SYSTEM_PROMPT,
            temperature = config.temperature
        )
    }

    suspend fun analyze(
        description: String,
        issueId: String? = null,
        projectId: String? = null
    ): Result<AnalysisResponse> {
        return try {
            if (description.isBlank()) {
                return Result.failure(Exception("Description required"))
            }
            if (description.length > 1000) {
                return Result.failure(Exception("Description too long (max 1000 characters)"))
            }

            var issue: Issue? = null
            var rules: List<WorkflowRule> = emptyList()
            var actualProjectId = projectId

            if (issueId != null) {
                issue = fetchIssueData(issueId)
                if (issue == null) {
                    return Result.failure(Exception("Issue $issueId not found"))
                }
                actualProjectId = issue.projectId
                logger.debug { "Fetched issue: ${issue.id} from project ${issue.projectId}" }
            }

            if (actualProjectId != null) {
                rules = fetchWorkflowRules(actualProjectId)
                logger.debug { "Fetched ${rules.size} workflow rules for project $actualProjectId" }
            }

            val prompt = if (issue != null && rules.isNotEmpty()) {
                buildEnrichedPrompt(issue, rules, description)
            } else {
                formatBasicPrompt(description, issueId)
            }
            logger.debug { "Formatted prompt for LLM" }

            val agent = testAgent ?: createAgent()

            val response: String = agent.run(prompt)
            logger.debug { "Received LLM response" }

            parseResponse(response, rules)
        } catch (e: Exception) {
            logger.error(e) { "Analysis failed: ${e.message}" }
            Result.failure(e)
        }
    }

    private suspend fun fetchIssueData(issueId: String): Issue? {
        return try {
            issueRepository.getIssue(issueId).getOrNull()
        } catch (e: Exception) {
            logger.warn(e) { "Failed to fetch issue $issueId: ${e.message}" }
            null
        }
    }

    private suspend fun fetchWorkflowRules(projectId: String): List<WorkflowRule> {
        return try {
            workflowRepository.getProjectRules(projectId).getOrNull() ?: emptyList()
        } catch (e: Exception) {
            logger.warn(e) { "Failed to fetch workflow rules for project $projectId: ${e.message}" }
            emptyList()
        }
    }

    private fun buildEnrichedPrompt(issue: Issue, rules: List<WorkflowRule>, description: String): String {
        return buildString {
            append("Issue: ${issue.id} - \"${issue.summary}\"\n")
            append("Current State: ${issue.state}\n")
            append("Assignee: ${issue.assignee ?: "(not set)"}\n")
            issue.priority?.let { append("Priority: $it\n") }
            issue.type?.let { append("Type: $it\n") }
            append("\n")

            append("User Action: $description\n")
            append("\n")

            append("Project Workflow Rules:\n")
            rules.take(15).forEachIndexed { index, rule ->
                append("${index + 1}. ${rule.name} (${rule.type})\n")
                rule.guard?.let { append("   - Guard: $it\n") }
                rule.action?.let { append("   - Action: $it\n") }
                rule.message?.let { append("   - Message: $it\n") }
                if (rule.requirements.isNotEmpty()) {
                    append("   - Requirements: ${rule.requirements.keys.joinToString()}\n")
                }
            }
            append("\n")

            append("Which rule(s) blocked this action? Respond with:\n")
            append("{\n")
            append("  \"explanation\": \"brief analysis of what happened\",\n")
            append("  \"suggestion\": \"how to fix it\",\n")
            append("  \"blockedByRules\": [\"rule name 1\", \"rule name 2\"]\n")
            append("}\n")
        }
    }

    private fun formatBasicPrompt(description: String, issueId: String?): String {
        return buildString {
            append("User reported: $description\n")
            issueId?.let { append("Issue ID: $it\n") }
            append("\nAnalyze this workflow error and provide:\n")
            append("1. What likely went wrong\n")
            append("2. Which workflow rule might have caused it\n")
            append("3. How to fix it")
        }
    }

    private fun parseResponse(response: String, availableRules: List<WorkflowRule>): Result<AnalysisResponse> {
        return try {
            val json = Json.parseToJsonElement(response).jsonObject
            val analysis = json["explanation"]?.jsonPrimitive?.content
                ?: json["analysis"]?.jsonPrimitive?.content
                ?: ""
            val suggestion = json["suggestion"]?.jsonPrimitive?.content
                ?: json["suggestedAction"]?.jsonPrimitive?.content
                ?: ""

            val blockedRuleNames = try {
                json["blockedByRules"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            } catch (e: Exception) {
                logger.debug { "No blockedByRules in response or parse failed" }
                emptyList()
            }

            val workflowRules = mapLlmRulesToWorkflowRules(blockedRuleNames, availableRules)

            logger.debug { "Successfully parsed JSON response with ${workflowRules.size} rules" }
            Result.success(
                AnalysisResponse(
                    explanation = analysis,
                    workflowRules = workflowRules,
                    suggestedActions = if (suggestion.isNotEmpty()) listOf(suggestion) else emptyList()
                )
            )
        } catch (e: Exception) {
            logger.warn { "Failed to parse JSON response, using raw text: ${e.message}" }
            Result.success(
                AnalysisResponse(
                    explanation = response,
                    workflowRules = emptyList(),
                    suggestedActions = listOf("Check the workflow rules for your project")
                )
            )
        }
    }

    private fun mapLlmRulesToWorkflowRules(
        llmRuleNames: List<String>,
        availableRules: List<WorkflowRule>
    ): List<WorkflowRuleInfo> {
        return llmRuleNames.mapNotNull { llmName ->
            val matchedRule = availableRules.find { rule ->
                rule.name.contains(llmName, ignoreCase = true) ||
                        llmName.contains(rule.name, ignoreCase = true)
            }

            if (matchedRule == null) {
                logger.warn { "Could not match LLM rule name '$llmName' to any available rules" }
                null
            } else {
                WorkflowRuleInfo(
                    name = matchedRule.name,
                    description = matchedRule.guard ?: matchedRule.message ?: matchedRule.action ?: "",
                    ruleUrl = generateRuleUrl(matchedRule)
                )
            }
        }
    }

    private fun generateRuleUrl(rule: WorkflowRule): String {
        return "${youtrackConfig.baseUrl}/admin/workflows/rules/${rule.id}"
    }

    companion object {
        private const val SYSTEM_PROMPT = """
You are a YouTrack workflow analyzer. When given an error description and workflow rules:
1. Identify what the user tried to do
2. Explain why it failed based on the workflow rules provided
3. Suggest how to fix it
4. Identify which specific rules blocked the action

Respond in JSON format.
"""
    }
}
