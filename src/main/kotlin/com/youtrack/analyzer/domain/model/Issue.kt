package com.youtrack.analyzer.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model representing a YouTrack issue
 */
data class Issue(
    val id: String,
    val projectId: String,
    val summary: String,
    val description: String? = null,
    val state: String,
    val fields: Map<String, Any?> = emptyMap(),
    val reporter: String? = null,
    val assignee: String? = null,
    val created: Instant,
    val updated: Instant,
    val resolved: Instant? = null,
    val tags: List<String> = emptyList(),
    val priority: String? = null,
    val type: String? = null,
    val links: List<IssueLink> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val customFields: Map<String, CustomFieldValue> = emptyMap()
) {
    /**
     * Check if issue is in a resolved state
     */
    fun isResolved(): Boolean = resolved != null

    /**
     * Check if issue is assigned
     */
    fun isAssigned(): Boolean = assignee != null

    /**
     * Get field value by name
     */
    fun getFieldValue(fieldName: String): Any? {
        return fields[fieldName] ?: customFields[fieldName]?.value
    }

    /**
     * Check if issue has a specific tag
     */
    fun hasTag(tag: String): Boolean = tags.contains(tag)
}

/**
 * Represents a link between issues
 */
data class IssueLink(
    val id: String,
    val type: String,
    val direction: LinkDirection,
    val targetIssueId: String,
    val targetIssueSummary: String? = null
)

/**
 * Direction of issue link
 */
enum class LinkDirection {
    INWARD,
    OUTWARD,
    BOTH
}

/**
 * Represents an issue attachment
 */
data class Attachment(
    val id: String,
    val name: String,
    val size: Long,
    val mimeType: String,
    val created: Instant,
    val author: String,
    val url: String
)

/**
 * Represents an issue comment
 */
data class Comment(
    val id: String,
    val text: String,
    val author: String,
    val created: Instant,
    val updated: Instant? = null,
    val deleted: Boolean = false
)

/**
 * Represents a custom field value
 */
data class CustomFieldValue(
    val name: String,
    val value: Any?,
    val type: CustomFieldType
)

/**
 * Types of custom fields
 */
enum class CustomFieldType {
    STRING,
    NUMBER,
    DATE,
    PERIOD,
    ENUM,
    USER,
    GROUP,
    VERSION,
    BUILD,
    STATE,
    OWNED_FIELD,
    MULTI_ENUM,
    MULTI_USER,
    MULTI_VERSION,
    MULTI_BUILD,
    MULTI_GROUP,
    MULTI_OWNED_FIELD
}