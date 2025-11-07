package com.youtrack.analyzer.infrastructure.cache

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Cache TTL (Time To Live) configuration for different data types
 */
object CacheTTL {
    // Workflow data - changes infrequently
    val WORKFLOW: Duration = 1.hours
    val WORKFLOW_RULES: Duration = 1.hours

    // Project data - relatively stable
    val PROJECT: Duration = 30.minutes
    val PROJECT_FIELDS: Duration = 1.hours

    // Issue data - changes more frequently
    val ISSUE: Duration = 5.minutes
    val ISSUE_ACTIVITIES: Duration = 2.minutes
    val ISSUE_SEARCH: Duration = 1.minutes

    // User data - stable
    val USER: Duration = 1.hours

    // Temporary data
    val TEMPORARY: Duration = 30.minutes

    // Analysis results
    val ANALYSIS_RESULT: Duration = 10.minutes

    // API responses
    val API_RESPONSE_SHORT: Duration = 1.minutes
    val API_RESPONSE_MEDIUM: Duration = 5.minutes
    val API_RESPONSE_LONG: Duration = 30.minutes

    /**
     * Get TTL for a specific cache type
     */
    fun forType(type: CacheType): Duration {
        return when (type) {
            CacheType.WORKFLOW -> WORKFLOW
            CacheType.WORKFLOW_RULES -> WORKFLOW_RULES
            CacheType.PROJECT -> PROJECT
            CacheType.PROJECT_FIELDS -> PROJECT_FIELDS
            CacheType.ISSUE -> ISSUE
            CacheType.ISSUE_ACTIVITIES -> ISSUE_ACTIVITIES
            CacheType.ISSUE_SEARCH -> ISSUE_SEARCH
            CacheType.USER -> USER
            CacheType.ANALYSIS_RESULT -> ANALYSIS_RESULT
            CacheType.TEMPORARY -> TEMPORARY
        }
    }
}

/**
 * Types of cached data
 */
enum class CacheType {
    WORKFLOW,
    WORKFLOW_RULES,
    PROJECT,
    PROJECT_FIELDS,
    ISSUE,
    ISSUE_ACTIVITIES,
    ISSUE_SEARCH,
    USER,
    ANALYSIS_RESULT,
    TEMPORARY
}