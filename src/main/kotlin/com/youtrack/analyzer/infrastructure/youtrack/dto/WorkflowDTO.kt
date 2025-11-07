package com.youtrack.analyzer.infrastructure.youtrack.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for YouTrack Workflow from REST API
 */
@Serializable
data class WorkflowDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("isEnabled")
    val isEnabled: Boolean = true,

    @SerialName("isAutoAttached")
    val isAutoAttached: Boolean = false,

    @SerialName("rules")
    val rules: List<WorkflowRuleDTO> = emptyList(),

    @SerialName("projects")
    val projects: List<ProjectRefDTO> = emptyList(),

    @SerialName("created")
    val created: Long? = null,

    @SerialName("updated")
    val updated: Long? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Workflow Rule
 */
@Serializable
data class WorkflowRuleDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("ruleType")
    val ruleType: String,

    @SerialName("guard")
    val guard: String? = null,

    @SerialName("title")
    val title: String? = null,

    @SerialName("body")
    val body: String? = null,

    @SerialName("script")
    val script: String? = null,

    @SerialName("requirements")
    val requirements: RequirementsDTO? = null,

    @SerialName("isEnabled")
    val isEnabled: Boolean = true,

    @SerialName("system")
    val system: Boolean = false,

    @SerialName("options")
    val options: List<String> = emptyList(),

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Rule Requirements
 */
@Serializable
data class RequirementsDTO(
    @SerialName("incompatibleActions")
    val incompatibleActions: List<String> = emptyList(),

    @SerialName("requiredFields")
    val requiredFields: List<String> = emptyList(),

    @SerialName("requiredProjects")
    val requiredProjects: List<String> = emptyList(),

    @SerialName("requiredIssueTypes")
    val requiredIssueTypes: List<String> = emptyList(),

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for State Machine Rule
 */
@Serializable
data class StateMachineRuleDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("fromState")
    val fromState: StateRefDTO? = null,

    @SerialName("toState")
    val toState: StateRefDTO? = null,

    @SerialName("guard")
    val guard: String? = null,

    @SerialName("actions")
    val actions: List<ActionDTO> = emptyList(),

    @SerialName("isEnabled")
    val isEnabled: Boolean = true,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for State Reference
 */
@Serializable
data class StateRefDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("isResolved")
    val isResolved: Boolean = false,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Action
 */
@Serializable
data class ActionDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("type")
    val type: String,

    @SerialName("field")
    val field: String? = null,

    @SerialName("value")
    @Contextual
    val value: Any? = null,

    @SerialName("script")
    val script: String? = null,

    @SerialName("\$type")
    val typeField: String? = null
)

/**
 * DTO for On-Change Rule
 */
@Serializable
data class OnChangeRuleDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("field")
    val field: String? = null,

    @SerialName("guard")
    val guard: String? = null,

    @SerialName("actions")
    val actions: List<ActionDTO> = emptyList(),

    @SerialName("isEnabled")
    val isEnabled: Boolean = true,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Schedule Rule
 */
@Serializable
data class ScheduleRuleDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("cron")
    val cron: String,

    @SerialName("guard")
    val guard: String? = null,

    @SerialName("actions")
    val actions: List<ActionDTO> = emptyList(),

    @SerialName("isEnabled")
    val isEnabled: Boolean = true,

    @SerialName("lastRun")
    val lastRun: Long? = null,

    @SerialName("nextRun")
    val nextRun: Long? = null,

    @SerialName("\$type")
    val type: String? = null
)