package com.youtrack.analyzer.ai

import ai.koog.agents.core.agent.AIAgent
import com.youtrack.analyzer.infrastructure.config.AIConfig
import com.youtrack.analyzer.infrastructure.config.YouTrackConfig
import com.youtrack.analyzer.domain.repository.IssueRepository
import com.youtrack.analyzer.domain.repository.WorkflowRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class WorkflowAnalyzerTest : FunSpec({

    lateinit var mockAgent: AIAgent<String, String>
    lateinit var config: AIConfig
    lateinit var issueRepository: IssueRepository
    lateinit var workflowRepository: WorkflowRepository
    lateinit var ytConfig: YouTrackConfig

    beforeEach {
        mockAgent = mockk<AIAgent<String, String>>()
        config = AIConfig(
            provider = "openai",
            model = "gpt-4-turbo-preview",
            apiKey = "test-key",
            maxTokens = 1500,
            temperature = 0.7
        )
        issueRepository = mockk(relaxed = true)
        workflowRepository = mockk(relaxed = true)
        ytConfig = YouTrackConfig(
            baseUrl = "http://localhost",
            token = "test",
            timeout = 10_000L // e.g., 10 seconds in ms
        )
    }

    afterEach {
        clearAllMocks()
    }

    test("analyzes workflow error successfully with valid JSON response") {
        runTest(timeout = 10.seconds) {
            val description = "Cannot move DEMO-42 to In Progress"
            val mockResponse = """
                {
                    "analysis": "The transition to 'In Progress' failed because the issue requires an assignee",
                    "suggestion": "Assign the issue to someone first",
                    "blockedByRules": ["Require Assignee on Start Progress"]
                }
            """.trimIndent()

            coEvery { mockAgent.run(any<String>()) } returns mockResponse

            val analyzer = WorkflowAnalyzer(
                testAgent = mockAgent,
                config = config,
                issueRepository = issueRepository,
                workflowRepository = workflowRepository,
                youtrackConfig = ytConfig
            )
            val result = analyzer.analyze(description)

            result.isSuccess shouldBe true
            val payload = result.getOrNull()!!
            payload.explanation shouldContain "requires an assignee"
            payload.suggestedActions.first() shouldContain "Assign the issue"
        }
    }

    test("rejects empty input") {
        runTest(timeout = 10.seconds) {
            val analyzer = WorkflowAnalyzer(mockAgent, config, issueRepository, workflowRepository, ytConfig)
            val result = analyzer.analyze("")
            result.isFailure shouldBe true
            result.exceptionOrNull()?.message shouldContain "Description required"
        }
    }

    test("rejects blank input") {
        runTest(timeout = 10.seconds) {
            val analyzer = WorkflowAnalyzer(mockAgent, config, issueRepository, workflowRepository, ytConfig)
            val result = analyzer.analyze("   ")
            result.isFailure shouldBe true
            result.exceptionOrNull()?.message shouldContain "Description required"
        }
    }

    test("rejects overly long input") {
        runTest(timeout = 10.seconds) {
            val analyzer = WorkflowAnalyzer(mockAgent, config, issueRepository, workflowRepository, ytConfig)
            val result = analyzer.analyze("a".repeat(1001))
            result.isFailure shouldBe true
            result.exceptionOrNull()?.message shouldContain "too long"
        }
    }

    test("validates input length is exactly 1000 characters is accepted") {
        runTest(timeout = 10.seconds) {
            val analyzer = WorkflowAnalyzer(mockAgent, config, issueRepository, workflowRepository, ytConfig)
            coEvery { mockAgent.run(any<String>()) } returns """{"analysis":"ok","suggestion":"ok"}"""
            val result = analyzer.analyze("a".repeat(1000))
            result.isSuccess shouldBe true
        }
    }

    test("validates input length of 999 characters is accepted") {
        runTest(timeout = 10.seconds) {
            val analyzer = WorkflowAnalyzer(mockAgent, config, issueRepository, workflowRepository, ytConfig)
            coEvery { mockAgent.run(any<String>()) } returns """{"analysis":"ok","suggestion":"ok"}"""
            val result = analyzer.analyze("a".repeat(999))
            result.isSuccess shouldBe true
        }
    }
})
