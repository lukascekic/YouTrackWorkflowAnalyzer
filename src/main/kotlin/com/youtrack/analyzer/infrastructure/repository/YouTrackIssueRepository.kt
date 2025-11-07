package com.youtrack.analyzer.infrastructure.repository

import com.youtrack.analyzer.domain.model.*
import com.youtrack.analyzer.domain.repository.IssueRepository
import com.youtrack.analyzer.infrastructure.youtrack.YouTrackApiService
import com.youtrack.analyzer.infrastructure.youtrack.simpleRetry
import org.slf4j.LoggerFactory

class YouTrackIssueRepository(
    private val apiService: YouTrackApiService
) : IssueRepository {

    private val logger = LoggerFactory.getLogger(YouTrackIssueRepository::class.java)

    override suspend fun getIssue(issueId: String): Result<Issue> {
        return simpleRetry {
            apiService.getIssue(issueId)
        }
    }

    override suspend fun getIssues(issueIds: List<String>): Result<List<Issue>> {
        if (issueIds.isEmpty()) {
            return Result.success(emptyList())
        }

        return simpleRetry {
            apiService.getIssues(issueIds)
        }
    }

    override suspend fun searchIssues(
        query: String,
        limit: Int,
        offset: Int
    ): Result<List<Issue>> {
        return simpleRetry {
            apiService.searchIssues(query, limit, offset)
        }
    }

    override suspend fun getProjectIssues(
        projectId: String,
        state: String?,
        assignee: String?,
        limit: Int,
        offset: Int
    ): Result<List<Issue>> {
        return simpleRetry {
            apiService.getProjectIssues(projectId, state, assignee, limit, offset)
        }
    }

    override suspend fun getIssueActivities(
        issueId: String,
        categories: List<String>?,
        limit: Int,
        cursor: String?
    ): Result<ActivityPage> {
        return simpleRetry {
            apiService.getIssueActivities(issueId, categories, limit, cursor)
        }
    }

    override suspend fun getRecentActivities(
        issueId: String,
        limit: Int
    ): Result<List<Activity>> {
        return simpleRetry {
            apiService.getIssueActivities(issueId, null, limit, null)
                .map { it.activities }
        }
    }

    override suspend fun exists(issueId: String): Result<Boolean> {
        val result = simpleRetry {
            apiService.getIssue(issueId).map { true }
        }
        return result.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.success(false) }
        )
    }

    override suspend fun invalidateCache(issueId: String): Result<Unit> {
        logger.debug("Cache invalidation requested for issue: $issueId")
        return Result.success(Unit)
    }

    override suspend fun invalidateProjectCache(projectId: String): Result<Unit> {
        logger.debug("Cache invalidation requested for project: $projectId")
        return Result.success(Unit)
    }
}