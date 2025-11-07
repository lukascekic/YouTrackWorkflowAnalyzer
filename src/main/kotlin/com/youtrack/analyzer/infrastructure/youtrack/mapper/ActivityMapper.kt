package com.youtrack.analyzer.infrastructure.youtrack.mapper

import com.youtrack.analyzer.domain.model.Activity
import com.youtrack.analyzer.domain.model.ActivityPage
import com.youtrack.analyzer.domain.model.ActivityType
import com.youtrack.analyzer.infrastructure.youtrack.dto.ActivitiesPageDTO
import com.youtrack.analyzer.infrastructure.youtrack.dto.ActivityDTO
import com.youtrack.analyzer.infrastructure.youtrack.dto.ActivityItemDTO
import kotlinx.datetime.Instant

/**
 * Mapper for converting Activity DTOs to domain models
 */
object ActivityMapper {

    /**
     * Convert ActivityDTO to Activity domain model
     */
    fun toDomain(dto: ActivityDTO): Activity {
        val (oldValue, newValue) = extractFieldValues(dto)

        return Activity(
            id = dto.id,
            issueId = dto.target?.id ?: "",
            timestamp = Instant.fromEpochMilliseconds(dto.timestamp),
            author = dto.author?.login ?: "System",
            type = determineActivityType(dto),
            field = dto.field?.presentation ?: dto.field?.customField?.name,
            oldValue = oldValue,
            newValue = newValue,
            added = dto.added.map { extractItemValue(it) },
            removed = dto.removed.map { extractItemValue(it) },
            target = dto.target?.idReadable,
            targetMember = dto.targetMember,
            description = buildDescription(dto)
        )
    }

    /**
     * Convert ActivitiesPageDTO to ActivityPage domain model
     */
    fun toDomainPage(dto: ActivitiesPageDTO): ActivityPage {
        return ActivityPage(
            activities = dto.activities.map { toDomain(it) },
            hasMore = dto.hasAfter,
            nextCursor = dto.afterCursor
        )
    }

    /**
     * Determine activity type from DTO
     */
    private fun determineActivityType(dto: ActivityDTO): ActivityType {
        return when {
            dto.category == "IssueCreatedCategory" -> ActivityType.ISSUE_CREATED
            dto.category == "CommentCategory" -> ActivityType.COMMENT_ADDED
            dto.category == "AttachmentCategory" -> ActivityType.ATTACHMENT_ADDED
            dto.field?.presentation == "State" -> ActivityType.STATE_CHANGE
            dto.field?.presentation == "Assignee" -> ActivityType.ASSIGNEE_CHANGE
            dto.category == "TagCategory" && dto.added.isNotEmpty() -> ActivityType.TAG_ADDED
            dto.category == "TagCategory" && dto.removed.isNotEmpty() -> ActivityType.TAG_REMOVED
            dto.category == "LinkCategory" && dto.added.isNotEmpty() -> ActivityType.LINK_ADDED
            dto.category == "LinkCategory" && dto.removed.isNotEmpty() -> ActivityType.LINK_REMOVED
            dto.category == "VoteCategory" && dto.added.isNotEmpty() -> ActivityType.VOTE_ADDED
            dto.category == "VoteCategory" && dto.removed.isNotEmpty() -> ActivityType.VOTE_REMOVED
            dto.category == "StarCategory" && dto.added.isNotEmpty() -> ActivityType.STAR_ADDED
            dto.category == "StarCategory" && dto.removed.isNotEmpty() -> ActivityType.STAR_REMOVED
            dto.category == "VisibilityCategory" -> ActivityType.VISIBILITY_CHANGE
            dto.category == "WorkItemCategory" -> ActivityType.WORK_ITEM_ADDED
            dto.field != null -> ActivityType.FIELD_UPDATE
            else -> ActivityType.CUSTOM
        }
    }

    /**
     * Extract field values from activity
     */
    private fun extractFieldValues(dto: ActivityDTO): Pair<Any?, Any?> {
        val oldValue = when {
            dto.removed.isNotEmpty() -> extractItemValue(dto.removed.first())
            else -> null
        }

        val newValue = when {
            dto.added.isNotEmpty() -> extractItemValue(dto.added.first())
            else -> null
        }

        return Pair(oldValue, newValue)
    }

    /**
     * Extract value from activity item
     */
    private fun extractItemValue(item: ActivityItemDTO): Any {
        return item.presentation
            ?: item.name
            ?: item.text
            ?: item.id
            ?: "Unknown"
    }

    /**
     * Build description for activity
     */
    private fun buildDescription(dto: ActivityDTO): String? {
        return when (dto.category) {
            "CommentCategory" -> dto.added.firstOrNull()?.text
            "AttachmentCategory" -> "Attachment: ${dto.added.firstOrNull()?.name}"
            "LinkCategory" -> {
                val action = if (dto.added.isNotEmpty()) "added" else "removed"
                "Link $action: ${dto.target?.idReadable}"
            }
            "WorkItemCategory" -> {
                val item = dto.added.firstOrNull()
                "Work logged: ${item?.presentation}"
            }
            else -> null
        }
    }
}