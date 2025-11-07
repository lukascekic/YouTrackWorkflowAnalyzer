package com.youtrack.analyzer.infrastructure.youtrack.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for YouTrack Issue from REST API
 */
@Serializable
data class IssueDTO(
    @SerialName("id")
    val id: String,

    @SerialName("idReadable")
    val idReadable: String? = null,

    @SerialName("summary")
    val summary: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("created")
    val created: Long,

    @SerialName("updated")
    val updated: Long,

    @SerialName("resolved")
    val resolved: Long? = null,

    @SerialName("project")
    val project: ProjectRefDTO? = null,

    @SerialName("reporter")
    val reporter: UserRefDTO? = null,

    @SerialName("updater")
    val updater: UserRefDTO? = null,

    @SerialName("fields")
    val fields: List<IssueFieldDTO> = emptyList(),

    @SerialName("tags")
    val tags: List<TagDTO> = emptyList(),

    @SerialName("links")
    val links: List<IssueLinkDTO> = emptyList(),

    @SerialName("attachments")
    val attachments: List<AttachmentDTO> = emptyList(),

    @SerialName("comments")
    val comments: List<CommentDTO> = emptyList(),

    @SerialName("customFields")
    val customFields: List<CustomFieldDTO> = emptyList(),

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Issue Field
 */
@Serializable
data class IssueFieldDTO(
    @SerialName("name")
    val name: String,

    @SerialName("value")
    val value: ValueDTO? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for generic field value
 */
@Serializable
data class ValueDTO(
    @SerialName("name")
    val name: String? = null,

    @SerialName("id")
    val id: String? = null,

    @SerialName("value")
    val value: String? = null,

    @SerialName("presentation")
    val presentation: String? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Project reference
 */
@Serializable
data class ProjectRefDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("shortName")
    val shortName: String? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for User reference
 */
@Serializable
data class UserRefDTO(
    @SerialName("id")
    val id: String,

    @SerialName("login")
    val login: String? = null,

    @SerialName("fullName")
    val fullName: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Tag
 */
@Serializable
data class TagDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("owner")
    val owner: UserRefDTO? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Issue Link
 */
@Serializable
data class IssueLinkDTO(
    @SerialName("id")
    val id: String,

    @SerialName("direction")
    val direction: String,

    @SerialName("linkType")
    val linkType: LinkTypeDTO? = null,

    @SerialName("issues")
    val issues: List<IssueRefDTO> = emptyList(),

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Link Type
 */
@Serializable
data class LinkTypeDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("sourceToTarget")
    val sourceToTarget: String? = null,

    @SerialName("targetToSource")
    val targetToSource: String? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Issue Reference
 */
@Serializable
data class IssueRefDTO(
    @SerialName("id")
    val id: String,

    @SerialName("idReadable")
    val idReadable: String? = null,

    @SerialName("summary")
    val summary: String? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Attachment
 */
@Serializable
data class AttachmentDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("author")
    val author: UserRefDTO? = null,

    @SerialName("created")
    val created: Long,

    @SerialName("updated")
    val updated: Long? = null,

    @SerialName("size")
    val size: Long,

    @SerialName("mimeType")
    val mimeType: String? = null,

    @SerialName("url")
    val url: String? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Comment
 */
@Serializable
data class CommentDTO(
    @SerialName("id")
    val id: String,

    @SerialName("author")
    val author: UserRefDTO? = null,

    @SerialName("text")
    val text: String,

    @SerialName("created")
    val created: Long,

    @SerialName("updated")
    val updated: Long? = null,

    @SerialName("deleted")
    val deleted: Boolean = false,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Custom Field
 */
@Serializable
data class CustomFieldDTO(
    @SerialName("name")
    val name: String,

    @SerialName("id")
    val id: String? = null,

    @SerialName("value")
    @Contextual
    val value: Any? = null,

    @SerialName("projectCustomField")
    val projectCustomField: ProjectCustomFieldDTO? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Project Custom Field
 */
@Serializable
data class ProjectCustomFieldDTO(
    @SerialName("id")
    val id: String,

    @SerialName("field")
    val field: FieldDTO? = null,

    @SerialName("canBeEmpty")
    val canBeEmpty: Boolean = true,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Field
 */
@Serializable
data class FieldDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("fieldType")
    val fieldType: FieldTypeDTO? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Field Type
 */
@Serializable
data class FieldTypeDTO(
    @SerialName("id")
    val id: String,

    @SerialName("isMultiValue")
    val isMultiValue: Boolean = false,

    @SerialName("\$type")
    val type: String? = null
)