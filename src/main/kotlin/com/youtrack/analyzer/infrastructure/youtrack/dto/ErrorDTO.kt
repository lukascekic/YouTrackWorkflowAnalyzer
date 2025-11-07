package com.youtrack.analyzer.infrastructure.youtrack.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for YouTrack error response
 */
@Serializable
data class ErrorResponseDTO(
    @SerialName("error")
    val error: String? = null,

    @SerialName("error_description")
    val errorDescription: String? = null,

    @SerialName("error_code")
    val errorCode: Int? = null,

    @SerialName("error_children")
    val errorChildren: List<ErrorChildDTO> = emptyList(),

    @SerialName("message")
    val message: String? = null,

    @SerialName("\$type")
    val type: String? = null
) {
    /**
     * Get the most descriptive error message available
     */
    fun getErrorMessage(): String {
        return errorDescription
            ?: message
            ?: error
            ?: errorChildren.firstOrNull()?.message
            ?: "Unknown error"
    }
}

/**
 * DTO for nested error information
 */
@Serializable
data class ErrorChildDTO(
    @SerialName("error")
    val error: String? = null,

    @SerialName("message")
    val message: String? = null,

    @SerialName("\$type")
    val type: String? = null
)