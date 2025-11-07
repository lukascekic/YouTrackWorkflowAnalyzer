package com.youtrack.analyzer.infrastructure.youtrack.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for YouTrack Project from REST API
 */
@Serializable
data class ProjectDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("shortName")
    val shortName: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("archived")
    val archived: Boolean = false,

    @SerialName("leader")
    val leader: UserRefDTO? = null,

    @SerialName("createdBy")
    val createdBy: UserRefDTO? = null,

    @SerialName("workflows")
    val workflows: List<WorkflowRefDTO> = emptyList(),

    @SerialName("fields")
    val fields: List<ProjectCustomFieldDTO> = emptyList(),

    @SerialName("issueTypes")
    val issueTypes: List<IssueTypeDTO> = emptyList(),

    @SerialName("created")
    val created: Long? = null,

    @SerialName("team")
    val team: TeamDTO? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Workflow Reference
 */
@Serializable
data class WorkflowRefDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Issue Type
 */
@Serializable
data class IssueTypeDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("icon")
    val icon: String? = null,

    @SerialName("color")
    val color: ColorDTO? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Color
 */
@Serializable
data class ColorDTO(
    @SerialName("id")
    val id: String,

    @SerialName("background")
    val background: String? = null,

    @SerialName("foreground")
    val foreground: String? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Team
 */
@Serializable
data class TeamDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("members")
    val members: List<TeamMemberDTO> = emptyList(),

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Team Member
 */
@Serializable
data class TeamMemberDTO(
    @SerialName("user")
    val user: UserRefDTO,

    @SerialName("roles")
    val roles: List<RoleDTO> = emptyList(),

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Role
 */
@Serializable
data class RoleDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("\$type")
    val type: String? = null
)

/**
 * DTO for Project List
 */
@Serializable
data class ProjectListDTO(
    @SerialName("projects")
    val projects: List<ProjectDTO> = emptyList(),

    @SerialName("skip")
    val skip: Int = 0,

    @SerialName("top")
    val top: Int = 0,

    @SerialName("total")
    val total: Int? = null,

    @SerialName("\$type")
    val type: String? = null
)