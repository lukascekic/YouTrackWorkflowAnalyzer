package com.youtrack.analyzer.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model representing issue activity/history
 */
data class Activity(
    val id: String,
    val issueId: String,
    val timestamp: Instant,
    val author: String,
    val type: ActivityType,
    val field: String? = null,
    val oldValue: Any? = null,
    val newValue: Any? = null,
    val added: List<Any> = emptyList(),
    val removed: List<Any> = emptyList(),
    val target: String? = null,
    val targetMember: String? = null,
    val description: String? = null
) {
    /**
     * Check if activity represents a state change
     */
    fun isStateChange(): Boolean = field == "State" || type == ActivityType.STATE_CHANGE

    /**
     * Check if activity represents an assignment change
     */
    fun isAssignmentChange(): Boolean = field == "Assignee" || type == ActivityType.ASSIGNEE_CHANGE

    /**
     * Check if activity represents a field update
     */
    fun isFieldUpdate(): Boolean = type == ActivityType.FIELD_UPDATE && field != null

    /**
     * Get a human-readable description of the activity
     */
    fun toHumanReadable(): String {
        return when (type) {
            ActivityType.ISSUE_CREATED -> "Issue created by $author"
            ActivityType.COMMENT_ADDED -> "Comment added by $author"
            ActivityType.ATTACHMENT_ADDED -> "Attachment added by $author"
            ActivityType.FIELD_UPDATE -> {
                when (field) {
                    null -> "Field updated by $author"
                    else -> "$field changed from $oldValue to $newValue by $author"
                }
            }
            ActivityType.STATE_CHANGE -> "State changed from $oldValue to $newValue by $author"
            ActivityType.ASSIGNEE_CHANGE -> "Assigned to $newValue by $author"
            ActivityType.TAG_ADDED -> "Tag ${added.firstOrNull()} added by $author"
            ActivityType.TAG_REMOVED -> "Tag ${removed.firstOrNull()} removed by $author"
            ActivityType.LINK_ADDED -> "Link added to $target by $author"
            ActivityType.LINK_REMOVED -> "Link removed to $target by $author"
            ActivityType.VOTE_ADDED -> "Vote added by $author"
            ActivityType.VOTE_REMOVED -> "Vote removed by $author"
            ActivityType.STAR_ADDED -> "Star added by $author"
            ActivityType.STAR_REMOVED -> "Star removed by $author"
            ActivityType.VISIBILITY_CHANGE -> "Visibility changed by $author"
            ActivityType.WORK_ITEM_ADDED -> "Work item added by $author"
            ActivityType.CUSTOM -> description ?: "Activity performed by $author"
        }
    }
}

/**
 * Types of activities
 */
enum class ActivityType {
    ISSUE_CREATED,
    COMMENT_ADDED,
    ATTACHMENT_ADDED,
    FIELD_UPDATE,
    STATE_CHANGE,
    ASSIGNEE_CHANGE,
    TAG_ADDED,
    TAG_REMOVED,
    LINK_ADDED,
    LINK_REMOVED,
    VOTE_ADDED,
    VOTE_REMOVED,
    STAR_ADDED,
    STAR_REMOVED,
    VISIBILITY_CHANGE,
    WORK_ITEM_ADDED,
    CUSTOM
}

/**
 * Represents a collection of activities with pagination
 */
data class ActivityPage(
    val activities: List<Activity>,
    val total: Int? = null,
    val hasMore: Boolean = false,
    val nextCursor: String? = null
)