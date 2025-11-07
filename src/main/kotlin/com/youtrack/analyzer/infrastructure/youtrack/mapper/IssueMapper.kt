package com.youtrack.analyzer.infrastructure.youtrack.mapper

import com.youtrack.analyzer.domain.model.*
import com.youtrack.analyzer.infrastructure.youtrack.dto.*
import kotlinx.datetime.Instant

/**
 * Mapper for converting Issue DTOs to domain models
 */
object IssueMapper {

    /**
     * Convert IssueDTO to Issue domain model
     */
    fun toDomain(dto: IssueDTO): Issue {
        val fields = dto.fields.associate { field ->
            field.name to field.value?.let { extractFieldValue(it) }
        }

        val customFields = dto.customFields.associate { field ->
            field.name to CustomFieldValue(
                name = field.name,
                value = field.value,
                type = determineFieldType(field.type ?: "")
            )
        }

        return Issue(
            id = dto.id,
            projectId = dto.project?.id ?: extractProjectId(dto.idReadable),
            summary = dto.summary,
            description = dto.description,
            state = extractState(dto.fields),
            fields = fields,
            reporter = dto.reporter?.login,
            assignee = extractAssignee(dto.fields),
            created = Instant.fromEpochMilliseconds(dto.created),
            updated = Instant.fromEpochMilliseconds(dto.updated),
            resolved = dto.resolved?.let { Instant.fromEpochMilliseconds(it) },
            tags = dto.tags.map { it.name },
            priority = extractPriority(dto.fields),
            type = extractIssueType(dto.fields),
            links = dto.links.map { toDomainLink(it) },
            attachments = dto.attachments.map { toDomainAttachment(it) },
            comments = dto.comments.map { toDomainComment(it) },
            customFields = customFields
        )
    }

    /**
     * Convert IssueLinkDTO to IssueLink domain model
     */
    private fun toDomainLink(dto: IssueLinkDTO): IssueLink {
        val targetIssue = dto.issues.firstOrNull()
        return IssueLink(
            id = dto.id,
            type = dto.linkType?.name ?: "Unknown",
            direction = mapLinkDirection(dto.direction),
            targetIssueId = targetIssue?.id ?: "",
            targetIssueSummary = targetIssue?.summary
        )
    }

    /**
     * Convert AttachmentDTO to Attachment domain model
     */
    private fun toDomainAttachment(dto: AttachmentDTO): Attachment {
        return Attachment(
            id = dto.id,
            name = dto.name,
            size = dto.size,
            mimeType = dto.mimeType ?: "application/octet-stream",
            created = Instant.fromEpochMilliseconds(dto.created),
            author = dto.author?.login ?: "Unknown",
            url = dto.url ?: ""
        )
    }

    /**
     * Convert CommentDTO to Comment domain model
     */
    private fun toDomainComment(dto: CommentDTO): Comment {
        return Comment(
            id = dto.id,
            text = dto.text,
            author = dto.author?.login ?: "Unknown",
            created = Instant.fromEpochMilliseconds(dto.created),
            updated = dto.updated?.let { Instant.fromEpochMilliseconds(it) },
            deleted = dto.deleted
        )
    }

    /**
     * Extract field value from ValueDTO
     */
    private fun extractFieldValue(value: ValueDTO): Any? {
        return value.value ?: value.name ?: value.presentation ?: value.id
    }

    /**
     * Extract project ID from readable ID
     */
    private fun extractProjectId(readableId: String?): String {
        return readableId?.substringBefore("-") ?: "UNKNOWN"
    }

    /**
     * Extract state from fields
     */
    private fun extractState(fields: List<IssueFieldDTO>): String {
        return fields
            .find { it.name.equals("State", ignoreCase = true) }
            ?.value?.name
            ?: "Unknown"
    }

    /**
     * Extract assignee from fields
     */
    private fun extractAssignee(fields: List<IssueFieldDTO>): String? {
        return fields
            .find { it.name.equals("Assignee", ignoreCase = true) }
            ?.value?.name
    }

    /**
     * Extract priority from fields
     */
    private fun extractPriority(fields: List<IssueFieldDTO>): String? {
        return fields
            .find { it.name.equals("Priority", ignoreCase = true) }
            ?.value?.name
    }

    /**
     * Extract issue type from fields
     */
    private fun extractIssueType(fields: List<IssueFieldDTO>): String? {
        return fields
            .find { it.name.equals("Type", ignoreCase = true) }
            ?.value?.name
    }

    /**
     * Map link direction string to enum
     */
    private fun mapLinkDirection(direction: String): LinkDirection {
        return when (direction.uppercase()) {
            "INWARD" -> LinkDirection.INWARD
            "OUTWARD" -> LinkDirection.OUTWARD
            "BOTH" -> LinkDirection.BOTH
            else -> LinkDirection.BOTH
        }
    }

    /**
     * Determine field type from type string
     */
    private fun determineFieldType(typeString: String): CustomFieldType {
        return when {
            typeString.contains("string", ignoreCase = true) -> CustomFieldType.STRING
            typeString.contains("number", ignoreCase = true) -> CustomFieldType.NUMBER
            typeString.contains("date", ignoreCase = true) -> CustomFieldType.DATE
            typeString.contains("period", ignoreCase = true) -> CustomFieldType.PERIOD
            typeString.contains("user", ignoreCase = true) && typeString.contains("multi", ignoreCase = true) -> CustomFieldType.MULTI_USER
            typeString.contains("user", ignoreCase = true) -> CustomFieldType.USER
            typeString.contains("enum", ignoreCase = true) && typeString.contains("multi", ignoreCase = true) -> CustomFieldType.MULTI_ENUM
            typeString.contains("enum", ignoreCase = true) -> CustomFieldType.ENUM
            typeString.contains("state", ignoreCase = true) -> CustomFieldType.STATE
            typeString.contains("version", ignoreCase = true) && typeString.contains("multi", ignoreCase = true) -> CustomFieldType.MULTI_VERSION
            typeString.contains("version", ignoreCase = true) -> CustomFieldType.VERSION
            typeString.contains("build", ignoreCase = true) && typeString.contains("multi", ignoreCase = true) -> CustomFieldType.MULTI_BUILD
            typeString.contains("build", ignoreCase = true) -> CustomFieldType.BUILD
            typeString.contains("group", ignoreCase = true) && typeString.contains("multi", ignoreCase = true) -> CustomFieldType.MULTI_GROUP
            typeString.contains("group", ignoreCase = true) -> CustomFieldType.GROUP
            typeString.contains("owned", ignoreCase = true) && typeString.contains("multi", ignoreCase = true) -> CustomFieldType.MULTI_OWNED_FIELD
            typeString.contains("owned", ignoreCase = true) -> CustomFieldType.OWNED_FIELD
            else -> CustomFieldType.STRING
        }
    }
}