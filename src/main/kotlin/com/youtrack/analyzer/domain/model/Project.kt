package com.youtrack.analyzer.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model representing a YouTrack project
 */
data class Project(
    val id: String,
    val shortName: String,
    val name: String,
    val description: String? = null,
    val leader: String? = null,
    val created: Instant? = null,
    val archived: Boolean = false,
    val workflows: List<String> = emptyList(),
    val fields: List<ProjectField> = emptyList(),
    val issueTypes: List<IssueType> = emptyList(),
    val states: List<State> = emptyList(),
    val priorities: List<Priority> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val members: List<ProjectMember> = emptyList()
) {
    /**
     * Check if project has a specific workflow
     */
    fun hasWorkflow(workflowId: String): Boolean = workflows.contains(workflowId)

    /**
     * Get field by name
     */
    fun getFieldByName(name: String): ProjectField? = fields.find { it.name == name }

    /**
     * Get all required fields
     */
    fun getRequiredFields(): List<ProjectField> = fields.filter { it.isRequired }

    /**
     * Check if user is a project member
     */
    fun isMember(userId: String): Boolean = members.any { it.userId == userId }

    /**
     * Get member role
     */
    fun getMemberRole(userId: String): ProjectRole? =
        members.find { it.userId == userId }?.role
}

/**
 * Represents a project field configuration
 */
data class ProjectField(
    val id: String,
    val name: String,
    val type: CustomFieldType,
    val isRequired: Boolean = false,
    val canBeEmpty: Boolean = true,
    val defaultValue: Any? = null,
    val bundle: FieldBundle? = null,
    val aliases: List<String> = emptyList()
)

/**
 * Represents a field bundle (set of possible values)
 */
data class FieldBundle(
    val id: String,
    val name: String,
    val values: List<BundleValue>
)

/**
 * Represents a value in a field bundle
 */
data class BundleValue(
    val id: String,
    val name: String,
    val description: String? = null,
    val ordinal: Int = 0,
    val color: String? = null,
    val isResolved: Boolean = false,
    val isArchived: Boolean = false
)

/**
 * Represents an issue type in a project
 */
data class IssueType(
    val id: String,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val color: String? = null
)

/**
 * Represents an issue state in a project
 */
data class State(
    val id: String,
    val name: String,
    val description: String? = null,
    val isResolved: Boolean = false,
    val color: String? = null,
    val ordinal: Int = 0
)

/**
 * Represents an issue priority in a project
 */
data class Priority(
    val id: String,
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val ordinal: Int = 0
)

/**
 * Represents a tag in a project
 */
data class Tag(
    val id: String,
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val owner: String? = null,
    val visibleFor: String? = null,
    val updateableBy: String? = null
)

/**
 * Represents a project member
 */
data class ProjectMember(
    val userId: String,
    val userName: String,
    val role: ProjectRole,
    val joinedAt: Instant? = null
)

/**
 * Project roles
 */
enum class ProjectRole {
    PROJECT_ADMIN,
    DEVELOPER,
    REPORTER,
    VIEWER,
    CUSTOM
}