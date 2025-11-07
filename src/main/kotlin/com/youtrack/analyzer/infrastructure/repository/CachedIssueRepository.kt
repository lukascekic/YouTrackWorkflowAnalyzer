package com.youtrack.analyzer.infrastructure.repository

import com.youtrack.analyzer.domain.model.Activity
import com.youtrack.analyzer.domain.model.ActivityPage
import com.youtrack.analyzer.domain.model.Issue
import com.youtrack.analyzer.domain.repository.IssueRepository
import com.youtrack.analyzer.infrastructure.cache.CacheManager
import com.youtrack.analyzer.infrastructure.cache.CacheTTL
import com.youtrack.analyzer.infrastructure.cache.RedisCacheManager
import org.slf4j.LoggerFactory

/**
 * Cached implementation of IssueRepository that wraps another repository with caching
 */
class CachedIssueRepository(
    private val delegate: IssueRepository,
    private val cacheManager: CacheManager
) : IssueRepository {

    private val logger = LoggerFactory.getLogger(CachedIssueRepository::class.java)

    override suspend fun getIssue(issueId: String): Result<Issue> {
        val cacheKey = RedisCacheManager.Companion.CacheKeys.issue(issueId)

        return try {
            Result.success(
                cacheManager.get(cacheKey, CacheTTL.ISSUE) {
                    delegate.getIssue(issueId).getOrThrow()
                }
            )
        } catch (e: Exception) {
            logger.error("Failed to get cached issue: $issueId", e)
            delegate.getIssue(issueId)
        }
    }

    override suspend fun getIssues(issueIds: List<String>): Result<List<Issue>> {
        // For multiple issues, fetch without caching for simplicity
        // Could be optimized to check cache for each issue individually
        return delegate.getIssues(issueIds)
    }

    override suspend fun searchIssues(query: String, limit: Int, offset: Int): Result<List<Issue>> {
        val cacheKey = "${RedisCacheManager.Companion.CacheKeys.projectIssues("search", query)}:$limit:$offset"

        return try {
            Result.success(
                cacheManager.get(cacheKey, CacheTTL.ISSUE_SEARCH) {
                    delegate.searchIssues(query, limit, offset).getOrThrow()
                }
            )
        } catch (e: Exception) {
            logger.error("Failed to get cached search results for: $query", e)
            delegate.searchIssues(query, limit, offset)
        }
    }

    override suspend fun getProjectIssues(
        projectId: String,
        state: String?,
        assignee: String?,
        limit: Int,
        offset: Int
    ): Result<List<Issue>> {
        val query = buildString {
            append("project:$projectId")
            state?.let { append(":state:$it") }
            assignee?.let { append(":assignee:$it") }
        }
        val cacheKey = "${RedisCacheManager.Companion.CacheKeys.projectIssues(projectId, query)}:$limit:$offset"

        return try {
            Result.success(
                cacheManager.get(cacheKey, CacheTTL.ISSUE) {
                    delegate.getProjectIssues(projectId, state, assignee, limit, offset).getOrThrow()
                }
            )
        } catch (e: Exception) {
            logger.error("Failed to get cached project issues for: $projectId", e)
            delegate.getProjectIssues(projectId, state, assignee, limit, offset)
        }
    }

    override suspend fun getIssueActivities(
        issueId: String,
        categories: List<String>?,
        limit: Int,
        cursor: String?
    ): Result<ActivityPage> {
        val cacheKey = RedisCacheManager.Companion.CacheKeys.activities(issueId, cursor)

        return try {
            Result.success(
                cacheManager.get(cacheKey, CacheTTL.ISSUE_ACTIVITIES) {
                    delegate.getIssueActivities(issueId, categories, limit, cursor).getOrThrow()
                }
            )
        } catch (e: Exception) {
            logger.error("Failed to get cached activities for: $issueId", e)
            delegate.getIssueActivities(issueId, categories, limit, cursor)
        }
    }

    override suspend fun getRecentActivities(issueId: String, limit: Int): Result<List<Activity>> {
        return delegate.getRecentActivities(issueId, limit)
    }

    override suspend fun exists(issueId: String): Result<Boolean> {
        return delegate.exists(issueId)
    }

    override suspend fun invalidateCache(issueId: String): Result<Unit> {
        return try {
            val cacheKey = RedisCacheManager.Companion.CacheKeys.issue(issueId)
            cacheManager.invalidate(cacheKey)
            logger.debug("Invalidated cache for issue: $issueId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to invalidate cache for issue: $issueId", e)
            Result.failure(e)
        }
    }

    override suspend fun invalidateProjectCache(projectId: String): Result<Unit> {
        return try {
            cacheManager.invalidatePattern("*project:$projectId*")
            logger.debug("Invalidated cache for project issues: $projectId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to invalidate project cache: $projectId", e)
            Result.failure(e)
        }
    }
}