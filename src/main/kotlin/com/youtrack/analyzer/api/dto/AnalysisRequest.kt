package com.youtrack.analyzer.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnalysisRequest(
    val errorMessage: String,
    val issueId: String? = null,
    val projectId: String? = null
)
