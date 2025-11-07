package com.youtrack.analyzer.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnalysisResponse(
    val explanation: String,
    val workflowRules: List<WorkflowRuleInfo>,
    val suggestedActions: List<String>
)

@Serializable
data class WorkflowRuleInfo(
    val name: String,
    val description: String,
    val ruleUrl: String? = null
)
