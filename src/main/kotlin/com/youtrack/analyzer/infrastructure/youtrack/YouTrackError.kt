package com.youtrack.analyzer.infrastructure.youtrack

import kotlin.time.Duration

/**
 * Sealed class representing various YouTrack API errors
 */
sealed class YouTrackError {
    abstract val message: String

    data class NetworkError(override val message: String, val cause: Throwable? = null) : YouTrackError()
    data class AuthenticationError(override val message: String) : YouTrackError()
    data class NotFoundError(val resource: String) : YouTrackError() {
        override val message = "Resource not found: $resource"
    }
    data class RateLimitError(val retryAfter: Duration) : YouTrackError() {
        override val message = "Rate limit exceeded. Retry after: $retryAfter"
    }
    data class ValidationError(val field: String, override val message: String) : YouTrackError()
    data class ServerError(override val message: String, val statusCode: Int) : YouTrackError()
    data class UnknownError(override val message: String) : YouTrackError()
}

/**
 * Extension function to convert an error to an exception
 */
fun YouTrackError.toException(): YouTrackApiException {
    return YouTrackApiException(message)
}