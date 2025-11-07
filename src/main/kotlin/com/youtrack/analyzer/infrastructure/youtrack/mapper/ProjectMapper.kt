package com.youtrack.analyzer.infrastructure.youtrack.mapper

import com.youtrack.analyzer.domain.model.*
import com.youtrack.analyzer.infrastructure.youtrack.dto.*
import kotlinx.datetime.Instant

/**
 * Mapper for converting Project DTOs to domain models
 */
object ProjectMapper {

    /**
     * Convert ProjectDTO to Project domain model
     */
    fun toDomain(dto: ProjectDTO): Project {
        return Project(
            id = dto.id,
            shortName = dto.shortName,
            name = dto.name,
            description = dto.description,
            leader = dto.leader?.login,
            created = dto.created?.let { Instant.fromEpochMilliseconds(it) },
            archived = dto.archived,
            workflows = dto.workflows.map { it.id },
            fields = dto.fields.map { toDomainField(it) },
            issueTypes = dto.issueTypes.map { toDomainIssueType(it) },
            states = extractStates(dto.fields),
            priorities = extractPriorities(dto.fields),
            tags = emptyList(), // Tags would need separate API call
            members = dto.team?.members?.map { toDomainMember(it) } ?: emptyList()
        )
    }

    /**
     * Convert ProjectCustomFieldDTO to ProjectField domain model
     */
    private fun toDomainField(dto: ProjectCustomFieldDTO): ProjectField {
        val field = dto.field ?: return ProjectField(
            id = dto.id,
            name = "Unknown",
            type = CustomFieldType.STRING,
            isRequired = !dto.canBeEmpty
        )

        return ProjectField(
            id = dto.id,
            name = field.name,
            type = determineFieldType(field.fieldType),
            isRequired = !dto.canBeEmpty,
            canBeEmpty = dto.canBeEmpty,
            defaultValue = null, // Would need additional API call
            bundle = null, // Would need additional API call
            aliases = emptyList()
        )
    }

    /**
     * Determine field type from FieldTypeDTO
     */
    private fun determineFieldType(fieldType: FieldTypeDTO?): CustomFieldType {
        if (fieldType == null) return CustomFieldType.STRING

        val typeString = fieldType.type ?: ""
        val isMulti = fieldType.isMultiValue

        return when {
            typeString.contains("string", ignoreCase = true) -> CustomFieldType.STRING
            typeString.contains("number", ignoreCase = true) -> CustomFieldType.NUMBER
            typeString.contains("date", ignoreCase = true) -> CustomFieldType.DATE
            typeString.contains("period", ignoreCase = true) -> CustomFieldType.PERIOD
            typeString.contains("user", ignoreCase = true) && isMulti -> CustomFieldType.MULTI_USER
            typeString.contains("user", ignoreCase = true) -> CustomFieldType.USER
            typeString.contains("enum", ignoreCase = true) && isMulti -> CustomFieldType.MULTI_ENUM
            typeString.contains("enum", ignoreCase = true) -> CustomFieldType.ENUM
            typeString.contains("state", ignoreCase = true) -> CustomFieldType.STATE
            typeString.contains("version", ignoreCase = true) && isMulti -> CustomFieldType.MULTI_VERSION
            typeString.contains("version", ignoreCase = true) -> CustomFieldType.VERSION
            typeString.contains("build", ignoreCase = true) && isMulti -> CustomFieldType.MULTI_BUILD
            typeString.contains("build", ignoreCase = true) -> CustomFieldType.BUILD
            typeString.contains("group", ignoreCase = true) && isMulti -> CustomFieldType.MULTI_GROUP
            typeString.contains("group", ignoreCase = true) -> CustomFieldType.GROUP
            typeString.contains("owned", ignoreCase = true) && isMulti -> CustomFieldType.MULTI_OWNED_FIELD
            typeString.contains("owned", ignoreCase = true) -> CustomFieldType.OWNED_FIELD
            else -> CustomFieldType.STRING
        }
    }

    /**
     * Convert IssueTypeDTO to IssueType domain model
     */
    private fun toDomainIssueType(dto: IssueTypeDTO): IssueType {
        return IssueType(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            icon = dto.icon,
            color = dto.color?.background
        )
    }

    /**
     * Convert TeamMemberDTO to ProjectMember domain model
     */
    private fun toDomainMember(dto: TeamMemberDTO): ProjectMember {
        val role = determineRole(dto.roles)
        return ProjectMember(
            userId = dto.user.id,
            userName = dto.user.login ?: dto.user.fullName ?: "Unknown",
            role = role,
            joinedAt = null // Not provided by API
        )
    }

    /**
     * Determine project role from roles list
     */
    private fun determineRole(roles: List<RoleDTO>): ProjectRole {
        val roleNames = roles.mapNotNull { it.name?.uppercase() }

        return when {
            roleNames.any { it.contains("ADMIN") } -> ProjectRole.PROJECT_ADMIN
            roleNames.any { it.contains("DEVELOPER") } -> ProjectRole.DEVELOPER
            roleNames.any { it.contains("REPORTER") } -> ProjectRole.REPORTER
            roleNames.any { it.contains("VIEWER") || it.contains("OBSERVER") } -> ProjectRole.VIEWER
            else -> ProjectRole.CUSTOM
        }
    }

    /**
     * Extract states from project fields
     * Note: In real implementation, this would require fetching the State bundle
     */
    private fun extractStates(fields: List<ProjectCustomFieldDTO>): List<State> {
        // This is a simplified implementation
        // In reality, we would need to fetch the field bundle to get all states
        return listOf(
            State(id = "1", name = "Open", isResolved = false, ordinal = 0),
            State(id = "2", name = "In Progress", isResolved = false, ordinal = 1),
            State(id = "3", name = "Fixed", isResolved = true, ordinal = 2),
            State(id = "4", name = "Verified", isResolved = true, ordinal = 3),
            State(id = "5", name = "Won't fix", isResolved = true, ordinal = 4)
        )
    }

    /**
     * Extract priorities from project fields
     * Note: In real implementation, this would require fetching the Priority bundle
     */
    private fun extractPriorities(fields: List<ProjectCustomFieldDTO>): List<Priority> {
        // This is a simplified implementation
        // In reality, we would need to fetch the field bundle to get all priorities
        return listOf(
            Priority(id = "1", name = "Critical", ordinal = 0),
            Priority(id = "2", name = "Major", ordinal = 1),
            Priority(id = "3", name = "Normal", ordinal = 2),
            Priority(id = "4", name = "Minor", ordinal = 3)
        )
    }
}