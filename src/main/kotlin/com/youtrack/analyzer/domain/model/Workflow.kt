package com.youtrack.analyzer.domain.model

import kotlinx.datetime.Instant

data class Workflow(
    val id: String,
    val name: String,
    val description: String? = null,
    val rules: List<WorkflowRule> = emptyList(),
    val isEnabled: Boolean = true,
    val isAutoAttached: Boolean = false,
    val projects: List<String> = emptyList(),
    val created: Instant? = null,
    val updated: Instant? = null
) {
    fun getRulesByType(type: RuleType): List<WorkflowRule> {
        return rules.filter { it.type == type }
    }

    fun hasActiveRules(): Boolean {
        return rules.any { it.isEnabled }
    }

    fun getStateMachineRules(): List<WorkflowRule> {
        return getRulesByType(RuleType.STATE_MACHINE)
    }

    fun getOnChangeRules(): List<WorkflowRule> {
        return getRulesByType(RuleType.ON_CHANGE)
    }

    fun getOnScheduleRules(): List<WorkflowRule> {
        return getRulesByType(RuleType.ON_SCHEDULE)
    }

    fun isAttachedToProject(projectId: String): Boolean {
        return projects.contains(projectId)
    }
}

data class WorkflowRule(
    val id: String,
    val name: String,
    val type: RuleType,
    val guard: String? = null,
    val action: String? = null,
    val requirements: Map<String, Any> = emptyMap(),
    val message: String? = null,
    val isEnabled: Boolean = true,
    val triggers: List<Trigger> = emptyList(),
    val conditions: List<Condition> = emptyList(),
    val actions: List<Action> = emptyList()
) {
    fun hasGuard(): Boolean = !guard.isNullOrBlank()

    fun hasTriggers(): Boolean = triggers.isNotEmpty()

    fun hasConditions(): Boolean = conditions.isNotEmpty() || hasGuard()

    fun getRequirement(key: String): Any? = requirements[key]

    fun requiresField(fieldName: String): Boolean {
        return requirements.containsKey(fieldName) ||
                guard?.contains(fieldName) == true ||
                action?.contains(fieldName) == true
    }
}

enum class RuleType {
    STATE_MACHINE,
    ON_CHANGE,
    ON_SCHEDULE,
    ACTION,
    CUSTOM_SCRIPT
}

data class Trigger(
    val type: TriggerType,
    val field: String? = null,
    val value: Any? = null,
    val schedule: String? = null
)

enum class TriggerType {
    FIELD_CHANGE,
    ISSUE_CREATED,
    ISSUE_UPDATED,
    ISSUE_RESOLVED,
    ISSUE_REOPENED,
    COMMENT_ADDED,
    ATTACHMENT_ADDED,
    LINK_ADDED,
    SCHEDULE,
    CUSTOM
}

data class Condition(
    val type: ConditionType,
    val field: String? = null,
    val operator: Operator? = null,
    val value: Any? = null,
    val expression: String? = null
)

enum class ConditionType {
    FIELD_VALUE,
    FIELD_CHANGED,
    USER_ROLE,
    USER_GROUP,
    TIME_TRACKING,
    ISSUE_STATE,
    CUSTOM_EXPRESSION
}

enum class Operator {
    EQUALS,
    NOT_EQUALS,
    CONTAINS,
    NOT_CONTAINS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_OR_EQUAL,
    LESS_OR_EQUAL,
    IN,
    NOT_IN,
    IS_NULL,
    IS_NOT_NULL,
    MATCHES,
    NOT_MATCHES
}

data class Action(
    val type: ActionType,
    val field: String? = null,
    val value: Any? = null,
    val message: String? = null,
    val notification: Notification? = null
)

enum class ActionType {
    SET_FIELD,
    ADD_TAG,
    REMOVE_TAG,
    ADD_COMMENT,
    SEND_EMAIL,
    CREATE_ISSUE,
    UPDATE_ISSUE,
    BLOCK_TRANSITION,
    ASSERT,
    RUN_COMMAND,
    CUSTOM_SCRIPT
}

data class Notification(
    val type: NotificationType,
    val recipients: List<String> = emptyList(),
    val subject: String? = null,
    val body: String? = null,
    val template: String? = null
)

enum class NotificationType {
    EMAIL,
    IN_APP,
    SLACK,
    WEBHOOK,
    CUSTOM
}