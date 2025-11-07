package com.youtrack.analyzer.api.routes

import com.youtrack.analyzer.ai.WorkflowAnalyzer
import com.youtrack.analyzer.api.dto.AnalysisRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("AnalysisRoutes")

fun Route.analyzeRoutes(analyzer: WorkflowAnalyzer) {
    post("/analyze") {
        try {
            val request = call.receive<AnalysisRequest>()
            logger.info { "Received analysis request for: ${request.errorMessage.take(50)}..." }

            // If issueId or projectId provided, fetch real YouTrack data
            // Otherwise, fall back to basic analysis without YouTrack context
            if (request.issueId == null && request.projectId == null) {
                logger.info { "Analysis request without issueId/projectId - using basic analysis" }
            }

            analyzer.analyze(
                description = request.errorMessage,
                issueId = request.issueId,
                projectId = request.projectId
            ).fold(
                onSuccess = { result ->
                    logger.info { "Analysis completed successfully" }
                    call.respond(HttpStatusCode.OK, result)
                },
                onFailure = { error ->
                    // Check if it's an "issue not found" error
                    if (error.message?.contains("not found", ignoreCase = true) == true) {
                        logger.warn { "Issue not found: ${error.message}" }
                        call.respond(
                            HttpStatusCode.NotFound,
                            mapOf("error" to error.message)
                        )
                    } else {
                        logger.error(error) { "Analysis failed: ${error.message}" }
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to (error.message ?: "Analysis failed"))
                        )
                    }
                }
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to process analysis request: ${e.message}" }
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (e.message ?: "Invalid request"))
            )
        }
    }
}
