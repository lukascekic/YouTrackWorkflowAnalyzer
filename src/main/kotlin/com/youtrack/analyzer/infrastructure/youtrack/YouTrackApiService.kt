package com.youtrack.analyzer.infrastructure.youtrack

import com.youtrack.analyzer.infrastructure.youtrack.dto.*
import com.youtrack.analyzer.infrastructure.youtrack.mapper.*
import com.youtrack.analyzer.domain.model.*
import io.ktor.client.call.*
import org.slf4j.LoggerFactory

class YouTrackApiService(
    private val client: YouTrackClient
) {
    private val logger = LoggerFactory.getLogger(YouTrackApiService::class.java)

    companion object {
        private const val API_PREFIX = "/api"
        private const val ADMIN_API_PREFIX = "/api/admin"
        private const val DEFAULT_FIELDS = "\$type,id,idReadable,summary,description,created,updated,resolved,reporter(id,login,fullName),project(id,name,shortName)"
        private const val ISSUE_FIELDS = "$DEFAULT_FIELDS,fields(name,value(name,id,presentation)),tags(id,name),links(id,direction,linkType(name),issues(id,idReadable,summary)),attachments(id,name,author,created,size,mimeType,url),comments(id,text,author,created,updated,deleted),customFields(id,name,value)"
        private const val WORKFLOW_FIELDS = "\$type,id,name,description,isEnabled,isAutoAttached,rules(id,name,ruleType,guard,title,body,script,requirements,isEnabled),projects(id,name,shortName)"
        private const val PROJECT_FIELDS = "\$type,id,name,shortName,description,archived,leader(id,login,fullName),workflows(id,name),fields(id,field(id,name,fieldType),canBeEmpty),issueTypes(id,name,description,icon,color),team(members(user(id,login,fullName),roles(id,name)))"
        private const val ACTIVITY_FIELDS = "\$type,id,timestamp,target(id,idReadable),author(id,login),field(presentation,customField(name)),added(id,name,presentation,text),removed(id,name,presentation,text),category,targetMember"
    }

    suspend fun getIssue(issueId: String): Result<Issue> {
        return try {
            logger.debug("Fetching issue: $issueId")
            val response = client.get(
                path = "$API_PREFIX/issues/$issueId",
                parameters = mapOf("fields" to ISSUE_FIELDS)
            )

            ResponseHandler.handleResponse<IssueDTO>(response)
                .map { dto ->
                    IssueMapper.toDomain(dto)
                }
        } catch (e: Exception) {
            logger.error("Failed to fetch issue $issueId", e)
            Result.failure(e)
        }
    }

    suspend fun getIssues(issueIds: List<String>): Result<List<Issue>> {
        return try {
            logger.debug("Fetching ${issueIds.size} issues")
            val query = issueIds.joinToString(" or ") { "id: $it" }

            val response = client.get(
                path = "$API_PREFIX/issues",
                parameters = mapOf(
                    "query" to query,
                    "fields" to ISSUE_FIELDS,
                    "\$top" to issueIds.size.toString()
                )
            )

            ResponseHandler.handleResponse<List<IssueDTO>>(response)
                .map { dtos ->
                    dtos.map { IssueMapper.toDomain(it) }
                }
        } catch (e: Exception) {
            logger.error("Failed to fetch issues", e)
            Result.failure(e)
        }
    }

    suspend fun getProjectWorkflows(projectId: String): Result<List<Workflow>> {
        return try {
            logger.debug("Fetching workflows for project: $projectId")
            val response = client.get(
                path = "$ADMIN_API_PREFIX/projects/$projectId/workflows",
                parameters = mapOf("fields" to WORKFLOW_FIELDS)
            )

            ResponseHandler.handleResponse<List<WorkflowDTO>>(response)
                .map { dtos ->
                    dtos.map { WorkflowMapper.toDomain(it) }
                }
        } catch (e: Exception) {
            logger.error("Failed to fetch workflows for project $projectId", e)
            Result.failure(e)
        }
    }

    suspend fun getWorkflow(workflowId: String): Result<Workflow> {
        return try {
            logger.debug("Fetching workflow: $workflowId")
            val response = client.get(
                path = "$ADMIN_API_PREFIX/workflows/$workflowId",
                parameters = mapOf("fields" to WORKFLOW_FIELDS)
            )

            ResponseHandler.handleResponse<WorkflowDTO>(response)
                .map { dto ->
                    WorkflowMapper.toDomain(dto)
                }
        } catch (e: Exception) {
            logger.error("Failed to fetch workflow $workflowId", e)
            Result.failure(e)
        }
    }

    suspend fun getWorkflowRules(workflowId: String): Result<List<WorkflowRule>> {
        return try {
            logger.debug("Fetching rules for workflow: $workflowId")
            val response = client.get(
                path = "$ADMIN_API_PREFIX/workflows/$workflowId/rules",
                parameters = mapOf(
                    "fields" to "id,name,ruleType,guard,title,body,script,requirements,isEnabled"
                )
            )

            ResponseHandler.handleResponse<List<WorkflowRuleDTO>>(response)
                .map { dtos ->
                    dtos.map { WorkflowMapper.toDomainRule(it) }
                }
        } catch (e: Exception) {
            logger.error("Failed to fetch workflow rules for $workflowId", e)
            Result.failure(e)
        }
    }

    suspend fun getIssueActivities(
        issueId: String,
        categories: List<String>? = null,
        limit: Int = 100,
        cursor: String? = null
    ): Result<ActivityPage> {
        return try {
            logger.debug("Fetching activities for issue: $issueId")

            val parameters = mutableMapOf(
                "fields" to ACTIVITY_FIELDS,
                "\$top" to limit.toString()
            )

            categories?.let {
                parameters["categories"] = it.joinToString(",")
            }

            cursor?.let {
                parameters["cursor"] = it
            }

            val response = client.get(
                path = "$API_PREFIX/issues/$issueId/activities",
                parameters = parameters
            )

            ResponseHandler.handleResponse<ActivitiesPageDTO>(response)
                .map { dto ->
                    ActivityMapper.toDomainPage(dto)
                }
        } catch (e: Exception) {
            logger.error("Failed to fetch activities for issue $issueId", e)
            Result.failure(e)
        }
    }

    suspend fun getProjects(
        archived: Boolean = false,
        limit: Int = 100,
        offset: Int = 0
    ): Result<List<Project>> {
        return try {
            logger.debug("Fetching projects (archived=$archived, limit=$limit, offset=$offset)")

            val query = if (!archived) "archived: false" else ""

            val response = client.get(
                path = "$ADMIN_API_PREFIX/projects",
                parameters = mapOf(
                    "fields" to PROJECT_FIELDS,
                    "query" to query,
                    "\$top" to limit.toString(),
                    "\$skip" to offset.toString()
                )
            )

            ResponseHandler.handleResponse<List<ProjectDTO>>(response)
                .map { dtos ->
                    dtos.map { ProjectMapper.toDomain(it) }
                }
        } catch (e: Exception) {
            logger.error("Failed to fetch projects", e)
            Result.failure(e)
        }
    }

    suspend fun getProject(projectId: String): Result<Project> {
        return try {
            logger.debug("Fetching project: $projectId")
            val response = client.get(
                path = "$ADMIN_API_PREFIX/projects/$projectId",
                parameters = mapOf("fields" to PROJECT_FIELDS)
            )

            ResponseHandler.handleResponse<ProjectDTO>(response)
                .map { dto ->
                    ProjectMapper.toDomain(dto)
                }
        } catch (e: Exception) {
            logger.error("Failed to fetch project $projectId", e)
            Result.failure(e)
        }
    }

    suspend fun searchIssues(
        query: String,
        limit: Int = 100,
        offset: Int = 0
    ): Result<List<Issue>> {
        return try {
            logger.debug("Searching issues with query: $query")
            val response = client.get(
                path = "$API_PREFIX/issues",
                parameters = mapOf(
                    "query" to query,
                    "fields" to ISSUE_FIELDS,
                    "\$top" to limit.toString(),
                    "\$skip" to offset.toString()
                )
            )

            ResponseHandler.handleResponse<List<IssueDTO>>(response)
                .map { dtos ->
                    dtos.map { IssueMapper.toDomain(it) }
                }
        } catch (e: Exception) {
            logger.error("Failed to search issues with query: $query", e)
            Result.failure(e)
        }
    }

    suspend fun getProjectIssues(
        projectId: String,
        state: String? = null,
        assignee: String? = null,
        limit: Int = 100,
        offset: Int = 0
    ): Result<List<Issue>> {
        val queryParts = mutableListOf("project: $projectId")
        state?.let { queryParts.add("state: $it") }
        assignee?.let { queryParts.add("assignee: $it") }

        val query = queryParts.joinToString(" and ")

        return searchIssues(query, limit, offset)
    }

    suspend fun testConnection(): Result<Boolean> {
        return try {
            logger.debug("Testing connection to YouTrack")
            val response = client.get(
                path = "$API_PREFIX/admin/users/me",
                parameters = mapOf("fields" to "id,login")
            )

            if (response.status.value in 200..299) {
                logger.info("Successfully connected to YouTrack")
                Result.success(true)
            } else {
                logger.error("Failed to connect to YouTrack: ${response.status}")
                Result.failure(
                    YouTrackApiException("Failed to connect: ${response.status}")
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to test connection", e)
            Result.failure(e)
        }
    }

    fun close() {
        client.close()
    }
}