package com.youtrack.analyzer.infrastructure.youtrack.mapper

import com.youtrack.analyzer.domain.model.*
import com.youtrack.analyzer.infrastructure.youtrack.dto.*
import kotlinx.datetime.Instant

/**
 * Mapper for converting Workflow DTOs to domain models
 */
object WorkflowMapper {

    /**
     * Convert WorkflowDTO to Workflow domain model
     */
    fun toDomain(dto: WorkflowDTO): Workflow {
        return Workflow(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            rules = dto.rules.map { toDomainRule(it) },
            isEnabled = dto.isEnabled,
            isAutoAttached = dto.isAutoAttached,
            projects = dto.projects.map { it.id },
            created = dto.created?.let { Instant.fromEpochMilliseconds(it) },
            updated = dto.updated?.let { Instant.fromEpochMilliseconds(it) }
        )
    }

    /**
     * Convert WorkflowRuleDTO to WorkflowRule domain model.
     * Simplified version - complex trigger/condition/action extraction removed for clarity.
     * Can be enhanced later if needed for workflow analysis.
     */
    fun toDomainRule(dto: WorkflowRuleDTO): WorkflowRule {
        val requirements = dto.requirements?.let { req ->
            buildMap<String, Any> {
                if (req.incompatibleActions.isNotEmpty()) {
                    put("incompatibleActions", req.incompatibleActions)
                }
                if (req.requiredFields.isNotEmpty()) {
                    put("requiredFields", req.requiredFields)
                }
                if (req.requiredProjects.isNotEmpty()) {
                    put("requiredProjects", req.requiredProjects)
                }
                if (req.requiredIssueTypes.isNotEmpty()) {
                    put("requiredIssueTypes", req.requiredIssueTypes)
                }
            }
        } ?: emptyMap()

        return WorkflowRule(
            id = dto.id,
            name = dto.name,
            type = mapRuleType(dto.ruleType),
            guard = dto.guard,
            action = dto.body ?: dto.script,
            requirements = requirements,
            message = dto.title,
            isEnabled = dto.isEnabled,
            triggers = emptyList(),
            conditions = emptyList(),
            actions = emptyList()
        )
    }

    /**
     * Convert StateMachineRuleDTO to WorkflowRule domain model
     */
    fun toDomainStateMachineRule(dto: StateMachineRuleDTO): WorkflowRule {
        val triggers = listOfNotNull(
            dto.fromState?.let {
                Trigger(
                    type = TriggerType.ISSUE_UPDATED,
                    field = "State",
                    value = it.name
                )
            }
        )

        val conditions = listOfNotNull(
            dto.fromState?.let {
                Condition(
                    type = ConditionType.FIELD_VALUE,
                    field = "State",
                    operator = Operator.EQUALS,
                    value = it.name
                )
            }
        )

        val actions = dto.actions.map { action ->
            Action(
                type = mapActionType(action.type),
                field = action.field,
                value = action.value,
                message = null
            )
        } + listOfNotNull(
            dto.toState?.let {
                Action(
                    type = ActionType.SET_FIELD,
                    field = "State",
                    value = it.name
                )
            }
        )

        return WorkflowRule(
            id = dto.id,
            name = dto.name,
            type = RuleType.STATE_MACHINE,
            guard = dto.guard,
            isEnabled = dto.isEnabled,
            triggers = triggers,
            conditions = conditions,
            actions = actions
        )
    }

    /**
     * Convert OnChangeRuleDTO to WorkflowRule domain model
     */
    fun toDomainOnChangeRule(dto: OnChangeRuleDTO): WorkflowRule {
        val triggers = listOfNotNull(
            dto.field?.let {
                Trigger(
                    type = TriggerType.FIELD_CHANGE,
                    field = it
                )
            }
        )

        val conditions = if (dto.guard != null) {
            listOf(
                Condition(
                    type = ConditionType.CUSTOM_EXPRESSION,
                    expression = dto.guard
                )
            )
        } else {
            emptyList()
        }

        val actions = dto.actions.map { action ->
            Action(
                type = mapActionType(action.type),
                field = action.field,
                value = action.value
            )
        }

        return WorkflowRule(
            id = dto.id,
            name = dto.name,
            type = RuleType.ON_CHANGE,
            guard = dto.guard,
            isEnabled = dto.isEnabled,
            triggers = triggers,
            conditions = conditions,
            actions = actions
        )
    }

    /**
     * Convert ScheduleRuleDTO to WorkflowRule domain model
     */
    fun toDomainScheduleRule(dto: ScheduleRuleDTO): WorkflowRule {
        val triggers = listOf(
            Trigger(
                type = TriggerType.SCHEDULE,
                schedule = dto.cron
            )
        )

        val conditions = if (dto.guard != null) {
            listOf(
                Condition(
                    type = ConditionType.CUSTOM_EXPRESSION,
                    expression = dto.guard
                )
            )
        } else {
            emptyList()
        }

        val actions = dto.actions.map { action ->
            Action(
                type = mapActionType(action.type),
                field = action.field,
                value = action.value
            )
        }

        return WorkflowRule(
            id = dto.id,
            name = dto.name,
            type = RuleType.ON_SCHEDULE,
            guard = dto.guard,
            isEnabled = dto.isEnabled,
            triggers = triggers,
            conditions = conditions,
            actions = actions
        )
    }

    /**
     * Map rule type string to enum
     */
    private fun mapRuleType(type: String): RuleType {
        return when (type.uppercase()) {
            "STATEMACHINE", "STATE_MACHINE" -> RuleType.STATE_MACHINE
            "ONCHANGE", "ON_CHANGE" -> RuleType.ON_CHANGE
            "ONSCHEDULE", "ON_SCHEDULE" -> RuleType.ON_SCHEDULE
            "ACTION" -> RuleType.ACTION
            "CUSTOM", "SCRIPT", "CUSTOM_SCRIPT" -> RuleType.CUSTOM_SCRIPT
            else -> RuleType.CUSTOM_SCRIPT
        }
    }

    /**
     * Map action type string to enum
     */
    private fun mapActionType(type: String): ActionType {
        return when (type.uppercase()) {
            "SETFIELD", "SET_FIELD" -> ActionType.SET_FIELD
            "ADDTAG", "ADD_TAG" -> ActionType.ADD_TAG
            "REMOVETAG", "REMOVE_TAG" -> ActionType.REMOVE_TAG
            "ADDCOMMENT", "ADD_COMMENT" -> ActionType.ADD_COMMENT
            "SENDEMAIL", "SEND_EMAIL" -> ActionType.SEND_EMAIL
            "CREATEISSUE", "CREATE_ISSUE" -> ActionType.CREATE_ISSUE
            "UPDATEISSUE", "UPDATE_ISSUE" -> ActionType.UPDATE_ISSUE
            "BLOCK", "BLOCKTRANSITION", "BLOCK_TRANSITION" -> ActionType.BLOCK_TRANSITION
            "ASSERT" -> ActionType.ASSERT
            "RUNCOMMAND", "RUN_COMMAND" -> ActionType.RUN_COMMAND
            "SCRIPT", "CUSTOM", "CUSTOM_SCRIPT" -> ActionType.CUSTOM_SCRIPT
            else -> ActionType.CUSTOM_SCRIPT
        }
    }
}