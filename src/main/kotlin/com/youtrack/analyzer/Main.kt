package com.youtrack.analyzer

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import com.youtrack.analyzer.ai.WorkflowAnalyzer
import com.youtrack.analyzer.api.routes.analyzeRoutes
import com.youtrack.analyzer.infrastructure.config.ConfigLoader
import com.youtrack.analyzer.infrastructure.youtrack.YouTrackClient
import com.youtrack.analyzer.infrastructure.youtrack.YouTrackApiService
import com.youtrack.analyzer.infrastructure.repository.YouTrackIssueRepository
import com.youtrack.analyzer.infrastructure.repository.YouTrackWorkflowRepository
import mu.KotlinLogging

private val logger = KotlinLogging.logger("Main")

fun main() {
    val config = ConfigLoader.load()

    logger.info { "Starting YouTrack Workflow Analyzer on port ${config.server.port}" }

    embeddedServer(
        Netty,
        port = config.server.port,
        host = config.server.host,
        module = { module(config) }
    ).start(wait = true)
}

fun Application.module(config: com.youtrack.analyzer.infrastructure.config.AppConfig) {
    install(ContentNegotiation) {
        json()
    }

    // Initialize YouTrack client and services
    val youtrackClient = YouTrackClient.fromConfig(config)
    logger.info { "YouTrack client initialized for ${config.youtrack.baseUrl}" }

    val apiService = YouTrackApiService(youtrackClient)
    logger.info { "YouTrack API service initialized" }

    // Initialize repositories
    val issueRepository = YouTrackIssueRepository(apiService)
    val workflowRepository = YouTrackWorkflowRepository(apiService)
    logger.info { "YouTrack repositories initialized" }

    // Initialize WorkflowAnalyzer with repositories
    val workflowAnalyzer = WorkflowAnalyzer(
        config = config.ai,
        issueRepository = issueRepository,
        workflowRepository = workflowRepository,
        youtrackConfig = config.youtrack
    )
    logger.info { "WorkflowAnalyzer initialized with YouTrack integration" }

    routing {
        get("/health") {
            call.respondText("OK")
        }

        get("/") {
            call.respondText("YouTrack Workflow Analyzer API")
        }

        // Register analysis routes
        this.analyzeRoutes(workflowAnalyzer)
    }

    logger.info { "Application configured successfully" }
}
