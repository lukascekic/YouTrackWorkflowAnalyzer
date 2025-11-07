package com.youtrack.analyzer.infrastructure.youtrack.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for Issue Activity from REST API
 */
@Serializable
data class ActivityDTO(
    @SerialName("id")
    val id: String,

    @SerialName("timestamp")
    val timestamp: Long,

    @SerialName("targetMember")
    val targetMember: String? = null,

    @SerialName("target")
    val target: ActivityTargetDTO? = null,

    @SerialName("author")
    val author: UserRefDTO? = null,

    @SerialName("field")
    val field: ActivityFieldDTO? = null,

    @SerialName("added")
    val added: List<ActivityItemDTO> = emptyList(),

    @SerialName("removed")
    val removed: List<ActivityItemDTO> = emptyList(),

    @SerialName("category")
    val category: String? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Activity Target
 */
@Serializable
data class ActivityTargetDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("idReadable")
    val idReadable: String? = null,

    @SerialName("summary")
    val summary: String? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Activity Field
 */
@Serializable
data class ActivityFieldDTO(
    @SerialName("id")
    val id: String,

    @SerialName("presentation")
    val presentation: String? = null,

    @SerialName("customField")
    val customField: CustomFieldRefDTO? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Custom Field Reference
 */
@Serializable
data class CustomFieldRefDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("fieldType")
    val fieldType: FieldTypeDTO? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Activity Item
 */
@Serializable
data class ActivityItemDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("presentation")
    val presentation: String? = null,

    @SerialName("text")
    val text: String? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Activities Page
 */
@Serializable
data class ActivitiesPageDTO(
    @SerialName("activities")
    val activities: List<ActivityDTO> = emptyList(),

    @SerialName("hasAfter")
    val hasAfter: Boolean = false,

    @SerialName("hasBefore")
    val hasBefore: Boolean = false,

    @SerialName("afterCursor")
    val afterCursor: String? = null,

    @SerialName("beforeCursor")
    val beforeCursor: String? = null,

    @SerialName("\$type")
    val type: String? = null
)