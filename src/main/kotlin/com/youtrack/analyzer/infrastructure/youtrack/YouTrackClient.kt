package com.youtrack.analyzer.infrastructure.youtrack

import com.youtrack.analyzer.infrastructure.config.AppConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Simplified YouTrack API client for interacting with YouTrack REST API.
 *
 * This implementation focuses on essential HTTP client functionality without
 * production-grade complexity like retry plugins, detailed logging, or
 * complex engine configuration. Retry logic is handled at the repository level.
 */
class YouTrackClient(
    private val config: YouTrackClientConfig
) {
    private val logger = LoggerFactory.getLogger(YouTrackClient::class.java)

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }

        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(config.token, "")
                }
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = config.timeout.inWholeMilliseconds
        }

        defaultRequest {
            url(config.baseUrl)
            contentType(ContentType.Application.Json)
        }
    }

    /**
     * Execute a GET request to YouTrack API
     */
    suspend fun get(path: String, parameters: Map<String, String> = emptyMap()): HttpResponse {
        logger.debug("GET request to: $path with parameters: $parameters")

        return try {
            httpClient.get(path) {
                parameters.forEach { (key, value) ->
                    parameter(key, value)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to execute GET request to $path", e)
            throw YouTrackApiException("Failed to execute GET request: ${e.message}", e)
        }
    }

    /**
     * Execute a POST request to YouTrack API
     */
    suspend fun post(path: String, body: Any? = null): HttpResponse {
        logger.debug("POST request to: $path")

        return try {
            httpClient.post(path) {
                body?.let {
                    contentType(ContentType.Application.Json)
                    setBody(it)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to execute POST request to $path", e)
            throw YouTrackApiException("Failed to execute POST request: ${e.message}", e)
        }
    }

    /**
     * Execute a PUT request to YouTrack API
     */
    suspend fun put(path: String, body: Any? = null): HttpResponse {
        logger.debug("PUT request to: $path")

        return try {
            httpClient.put(path) {
                body?.let {
                    contentType(ContentType.Application.Json)
                    setBody(it)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to execute PUT request to $path", e)
            throw YouTrackApiException("Failed to execute PUT request: ${e.message}", e)
        }
    }

    /**
     * Execute a DELETE request to YouTrack API
     */
    suspend fun delete(path: String): HttpResponse {
        logger.debug("DELETE request to: $path")

        return try {
            httpClient.delete(path)
        } catch (e: Exception) {
            logger.error("Failed to execute DELETE request to $path", e)
            throw YouTrackApiException("Failed to execute DELETE request: ${e.message}", e)
        }
    }

    /**
     * Close the HTTP client and release resources
     */
    fun close() {
        httpClient.close()
    }

    companion object {
        /**
         * Create a YouTrackClient from application configuration
         */
        fun fromConfig(appConfig: AppConfig): YouTrackClient {
            val config = YouTrackClientConfig(
                baseUrl = appConfig.youtrack.baseUrl,
                token = appConfig.youtrack.token,
                timeout = appConfig.youtrack.timeout?.seconds ?: 30.seconds
            )
            return YouTrackClient(config)
        }
    }
}

/**
 * Simplified configuration for YouTrack API client.
 * Contains only essential settings for internship scope.
 */
data class YouTrackClientConfig(
    val baseUrl: String,
    val token: String,
    val timeout: Duration = 30.seconds
)

/**
 * Exception thrown when YouTrack API operations fail
 */
class YouTrackApiException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
