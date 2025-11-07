package com.youtrack.analyzer.infrastructure.youtrack

import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

/**
 * Handles YouTrack API responses and converts them to Result types
 */
object ResponseHandler {
    @PublishedApi
    internal val logger = LoggerFactory.getLogger(ResponseHandler::class.java)

    /**
     * Process HTTP response and convert to Result type
     */
    suspend inline fun <reified T> handleResponse(response: HttpResponse): Result<T> {
        return try {
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created, HttpStatusCode.Accepted -> {
                    try {
                        val data = response.body<T>()
                        Result.success(data)
                    } catch (e: SerializationException) {
                        logger.error("Failed to deserialize response: ${response.bodyAsText()}", e)
                        Result.failure(
                            YouTrackError.ValidationError(
                                "response",
                                "Failed to parse response: ${e.message}"
                            ).toException()
                        )
                    }
                }

                HttpStatusCode.NoContent -> {
                    @Suppress("UNCHECKED_CAST")
                    Result.success(Unit as T)
                }

                HttpStatusCode.Unauthorized -> {
                    logger.error("Authentication failed: ${response.status}")
                    Result.failure(
                        YouTrackError.AuthenticationError(
                            "Invalid or expired authentication token"
                        ).toException()
                    )
                }

                HttpStatusCode.Forbidden -> {
                    logger.error("Access forbidden: ${response.status}")
                    Result.failure(
                        YouTrackError.AuthenticationError(
                            "Access denied to the requested resource"
                        ).toException()
                    )
                }

                HttpStatusCode.NotFound -> {
                    val path = response.request.url.encodedPath
                    logger.warn("Resource not found: $path")
                    Result.failure(
                        YouTrackError.NotFoundError(path).toException()
                    )
                }

                HttpStatusCode.TooManyRequests -> {
                    val retryAfter = response.headers["Retry-After"]?.toLongOrNull()?.seconds
                        ?: 60.seconds
                    logger.warn("Rate limit exceeded, retry after: $retryAfter")
                    Result.failure(
                        YouTrackError.RateLimitError(retryAfter).toException()
                    )
                }

                HttpStatusCode.BadRequest -> {
                    val errorMessage = try {
                        response.bodyAsText()
                    } catch (e: Exception) {
                        "Bad request"
                    }
                    logger.error("Bad request: $errorMessage")
                    Result.failure(
                        YouTrackError.ValidationError(
                            "request",
                            errorMessage
                        ).toException()
                    )
                }

                HttpStatusCode.InternalServerError,
                HttpStatusCode.BadGateway,
                HttpStatusCode.ServiceUnavailable,
                HttpStatusCode.GatewayTimeout -> {
                    val errorMessage = try {
                        response.bodyAsText()
                    } catch (e: Exception) {
                        "Server error: ${response.status}"
                    }
                    logger.error("Server error: ${response.status} - $errorMessage")
                    Result.failure(
                        YouTrackError.ServerError(
                            errorMessage,
                            response.status.value
                        ).toException()
                    )
                }

                else -> {
                    val errorMessage = try {
                        response.bodyAsText()
                    } catch (e: Exception) {
                        "Unexpected status: ${response.status}"
                    }
                    logger.error("Unexpected response: ${response.status} - $errorMessage")
                    Result.failure(
                        YouTrackError.UnknownError(
                            "Unexpected response status: ${response.status} - $errorMessage"
                        ).toException()
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("Error processing response", e)
            Result.failure(
                YouTrackError.UnknownError(
                    "Error processing response: ${e.message}"
                ).toException()
            )
        }
    }

    /**
     * Process HTTP response without expecting a body
     */
    suspend fun handleEmptyResponse(response: HttpResponse): Result<Unit> {
        return handleResponse<Unit>(response)
    }
}