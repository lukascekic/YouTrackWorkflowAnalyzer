package com.youtrack.analyzer.domain.repository

import com.youtrack.analyzer.domain.model.Activity
import com.youtrack.analyzer.domain.model.ActivityPage
import com.youtrack.analyzer.domain.model.Issue

/**
 * Repository interface for Issue operations
 */
interface IssueRepository {

    /**
     * Get an issue by ID
     */
    suspend fun getIssue(issueId: String): Result<Issue>

    /**
     * Get multiple issues by IDs
     */
    suspend fun getIssues(issueIds: List<String>): Result<List<Issue>>

    /**
     * Search issues using query
     */
    suspend fun searchIssues(
        query: String,
        limit: Int = 100,
        offset: Int = 0
    ): Result<List<Issue>>

    /**
     * Get issues by project ID
     */
    suspend fun getProjectIssues(
        projectId: String,
        state: String? = null,
        assignee: String? = null,
        limit: Int = 100,
        offset: Int = 0
    ): Result<List<Issue>>

    /**
     * Get issue activities (history)
     */
    suspend fun getIssueActivities(
        issueId: String,
        categories: List<String>? = null,
        limit: Int = 100,
        cursor: String? = null
    ): Result<ActivityPage>

    /**
     * Get recent activities for an issue
     */
    suspend fun getRecentActivities(
        issueId: String,
        limit: Int = 10
    ): Result<List<Activity>>

    /**
     * Check if issue exists
     */
    suspend fun exists(issueId: String): Result<Boolean>

    /**
     * Invalidate cached issue data
     */
    suspend fun invalidateCache(issueId: String): Result<Unit>

    /**
     * Invalidate all cached issues for a project
     */
    suspend fun invalidateProjectCache(projectId: String): Result<Unit>
}