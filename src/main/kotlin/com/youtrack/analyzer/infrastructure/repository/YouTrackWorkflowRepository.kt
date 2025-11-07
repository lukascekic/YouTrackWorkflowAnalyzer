package com.youtrack.analyzer.infrastructure.repository

import com.youtrack.analyzer.domain.model.RuleType
import com.youtrack.analyzer.domain.model.Workflow
import com.youtrack.analyzer.domain.model.WorkflowRule
import com.youtrack.analyzer.domain.repository.WorkflowRepository
import com.youtrack.analyzer.infrastructure.youtrack.YouTrackApiService
import com.youtrack.analyzer.infrastructure.youtrack.simpleRetry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory

class YouTrackWorkflowRepository(
    private val apiService: YouTrackApiService
) : WorkflowRepository {

    private val logger = LoggerFactory.getLogger(YouTrackWorkflowRepository::class.java)

    override suspend fun getProjectWorkflows(projectId: String): Result<List<Workflow>> {
        return simpleRetry {
            apiService.getProjectWorkflows(projectId)
        }
    }

    override suspend fun getWorkflow(workflowId: String): Result<Workflow> {
        return simpleRetry {
            apiService.getWorkflow(workflowId)
        }
    }

    override suspend fun getWorkflowRules(workflowId: String): Result<List<WorkflowRule>> {
        return simpleRetry {
            apiService.getWorkflowRules(workflowId)
        }
    }

    override suspend fun getProjectRules(projectId: String): Result<List<WorkflowRule>> {
        return simpleRetry {
            coroutineScope {
                val workflowsResult = apiService.getProjectWorkflows(projectId)

                workflowsResult.mapCatching { workflows ->
                    val ruleResults = workflows.map { workflow ->
                        async {
                            apiService.getWorkflowRules(workflow.id)
                        }
                    }.awaitAll()

                    ruleResults.flatMap { result ->
                        result.getOrElse { emptyList() }
                    }
                }
            }
        }
    }

    override suspend fun getProjectRulesByType(
        projectId: String,
        ruleType: String
    ): Result<List<WorkflowRule>> {
        return getProjectRules(projectId).map { rules ->
            rules.filter { rule ->
                rule.type.name.equals(ruleType, ignoreCase = true)
            }
        }
    }

    override suspend fun getStateMachineRules(projectId: String): Result<List<WorkflowRule>> {
        return getProjectRulesByType(projectId, RuleType.STATE_MACHINE.name)
    }

    override suspend fun getOnChangeRules(projectId: String): Result<List<WorkflowRule>> {
        return getProjectRulesByType(projectId, RuleType.ON_CHANGE.name)
    }

    override suspend fun exists(workflowId: String): Result<Boolean> {
        val result = simpleRetry {
            apiService.getWorkflow(workflowId).map { true }
        }
        return result.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.success(false) }
        )
    }

    override suspend fun invalidateCache(workflowId: String): Result<Unit> {
        logger.debug("Cache invalidation requested for workflow: $workflowId")
        return Result.success(Unit)
    }

    override suspend fun invalidateProjectCache(projectId: String): Result<Unit> {
        logger.debug("Cache invalidation requested for project workflows: $projectId")
        return Result.success(Unit)
    }
}