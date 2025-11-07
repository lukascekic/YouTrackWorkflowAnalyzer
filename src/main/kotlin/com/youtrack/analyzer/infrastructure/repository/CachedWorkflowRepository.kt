package com.youtrack.analyzer.infrastructure.repository

import com.youtrack.analyzer.domain.model.Workflow
import com.youtrack.analyzer.domain.model.WorkflowRule
import com.youtrack.analyzer.domain.repository.WorkflowRepository
import com.youtrack.analyzer.infrastructure.cache.CacheManager
import com.youtrack.analyzer.infrastructure.cache.CacheTTL
import com.youtrack.analyzer.infrastructure.cache.RedisCacheManager
import org.slf4j.LoggerFactory

/**
 * Cached implementation of WorkflowRepository that wraps another repository with caching
 */
class CachedWorkflowRepository(
    private val delegate: WorkflowRepository,
    private val cacheManager: CacheManager
) : WorkflowRepository {

    private val logger = LoggerFactory.getLogger(CachedWorkflowRepository::class.java)

    override suspend fun getProjectWorkflows(projectId: String): Result<List<Workflow>> {
        val cacheKey = RedisCacheManager.Companion.CacheKeys.workflow(projectId)

        return try {
            Result.success(
                cacheManager.get(cacheKey, CacheTTL.WORKFLOW) {
                    delegate.getProjectWorkflows(projectId).getOrThrow()
                }
            )
        } catch (e: Exception) {
            logger.error("Failed to get cached workflows for project: $projectId", e)
            delegate.getProjectWorkflows(projectId)
        }
    }

    override suspend fun getWorkflow(workflowId: String): Result<Workflow> {
        val cacheKey = "workflow:single:$workflowId"

        return try {
            Result.success(
                cacheManager.get(cacheKey, CacheTTL.WORKFLOW) {
                    delegate.getWorkflow(workflowId).getOrThrow()
                }
            )
        } catch (e: Exception) {
            logger.error("Failed to get cached workflow: $workflowId", e)
            delegate.getWorkflow(workflowId)
        }
    }

    override suspend fun getWorkflowRules(workflowId: String): Result<List<WorkflowRule>> {
        val cacheKey = RedisCacheManager.Companion.CacheKeys.workflowRules(workflowId)

        return try {
            Result.success(
                cacheManager.get(cacheKey, CacheTTL.WORKFLOW_RULES) {
                    delegate.getWorkflowRules(workflowId).getOrThrow()
                }
            )
        } catch (e: Exception) {
            logger.error("Failed to get cached workflow rules: $workflowId", e)
            delegate.getWorkflowRules(workflowId)
        }
    }

    override suspend fun getProjectRules(projectId: String): Result<List<WorkflowRule>> {
        val cacheKey = "workflow:rules:project:$projectId"

        return try {
            Result.success(
                cacheManager.get(cacheKey, CacheTTL.WORKFLOW_RULES) {
                    delegate.getProjectRules(projectId).getOrThrow()
                }
            )
        } catch (e: Exception) {
            logger.error("Failed to get cached project rules: $projectId", e)
            delegate.getProjectRules(projectId)
        }
    }

    override suspend fun getProjectRulesByType(
        projectId: String,
        ruleType: String
    ): Result<List<WorkflowRule>> {
        val cacheKey = "workflow:rules:project:$projectId:type:$ruleType"

        return try {
            Result.success(
                cacheManager.get(cacheKey, CacheTTL.WORKFLOW_RULES) {
                    delegate.getProjectRulesByType(projectId, ruleType).getOrThrow()
                }
            )
        } catch (e: Exception) {
            logger.error("Failed to get cached project rules by type: $projectId:$ruleType", e)
            delegate.getProjectRulesByType(projectId, ruleType)
        }
    }

    override suspend fun getStateMachineRules(projectId: String): Result<List<WorkflowRule>> {
        return delegate.getStateMachineRules(projectId)
    }

    override suspend fun getOnChangeRules(projectId: String): Result<List<WorkflowRule>> {
        return delegate.getOnChangeRules(projectId)
    }

    override suspend fun exists(workflowId: String): Result<Boolean> {
        return delegate.exists(workflowId)
    }

    override suspend fun invalidateCache(workflowId: String): Result<Unit> {
        return try {
            cacheManager.invalidatePattern("*workflow*:*$workflowId*")
            logger.debug("Invalidated cache for workflow: $workflowId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to invalidate workflow cache: $workflowId", e)
            Result.failure(e)
        }
    }

    override suspend fun invalidateProjectCache(projectId: String): Result<Unit> {
        return try {
            cacheManager.invalidatePattern("*workflow*:*$projectId*")
            logger.debug("Invalidated cache for project workflows: $projectId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to invalidate project workflow cache: $projectId", e)
            Result.failure(e)
        }
    }
}