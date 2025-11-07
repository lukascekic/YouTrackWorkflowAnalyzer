package com.youtrack.analyzer.domain.repository

import com.youtrack.analyzer.domain.model.Workflow
import com.youtrack.analyzer.domain.model.WorkflowRule

/**
 * Repository interface for Workflow operations
 */
interface WorkflowRepository {

    /**
     * Get all workflows for a project
     */
    suspend fun getProjectWorkflows(projectId: String): Result<List<Workflow>>

    /**
     * Get a workflow by ID
     */
    suspend fun getWorkflow(workflowId: String): Result<Workflow>

    /**
     * Get workflow rules
     */
    suspend fun getWorkflowRules(workflowId: String): Result<List<WorkflowRule>>

    /**
     * Get all rules for a project
     */
    suspend fun getProjectRules(projectId: String): Result<List<WorkflowRule>>

    /**
     * Get rules of a specific type for a project
     */
    suspend fun getProjectRulesByType(
        projectId: String,
        ruleType: String
    ): Result<List<WorkflowRule>>

    /**
     * Get state machine rules for a project
     */
    suspend fun getStateMachineRules(projectId: String): Result<List<WorkflowRule>>

    /**
     * Get on-change rules for a project
     */
    suspend fun getOnChangeRules(projectId: String): Result<List<WorkflowRule>>

    /**
     * Check if workflow exists
     */
    suspend fun exists(workflowId: String): Result<Boolean>

    /**
     * Invalidate cached workflow data
     */
    suspend fun invalidateCache(workflowId: String): Result<Unit>

    /**
     * Invalidate all cached workflows for a project
     */
    suspend fun invalidateProjectCache(projectId: String): Result<Unit>
}