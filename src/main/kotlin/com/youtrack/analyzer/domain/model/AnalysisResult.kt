package com.youtrack.analyzer.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model representing the result of workflow analysis
 */
data class AnalysisResult(
    val id: String,
    val issueId: String,
    val projectId: String,
    val timestamp: Instant,
    val violations: List<RuleViolation> = emptyList(),
    val suggestions: List<Suggestion> = emptyList(),
    val blockedTransitions: List<BlockedTransition> = emptyList(),
    val performance: PerformanceMetrics? = null,
    val summary: AnalysisSummary? = null,
    val metadata: Map<String, Any> = emptyMap()
) {
    /**
     * Check if analysis found any issues
     */
    fun hasIssues(): Boolean = violations.isNotEmpty() || blockedTransitions.isNotEmpty()

    /**
     * Check if analysis has critical violations
     */
    fun hasCriticalViolations(): Boolean = violations.any { it.severity == Severity.CRITICAL }

    /**
     * Get violations by severity
     */
    fun getViolationsBySeverity(severity: Severity): List<RuleViolation> {
        return violations.filter { it.severity == severity }
    }

    /**
     * Get all critical and high severity violations
     */
    fun getHighPriorityViolations(): List<RuleViolation> {
        return violations.filter {
            it.severity == Severity.CRITICAL || it.severity == Severity.HIGH
        }
    }

    /**
     * Calculate overall health score (0-100)
     */
    fun calculateHealthScore(): Int {
        if (violations.isEmpty()) return 100

        val weights = mapOf(
            Severity.CRITICAL to 25,
            Severity.HIGH to 15,
            Severity.MEDIUM to 10,
            Severity.LOW to 5,
            Severity.INFO to 1
        )

        val totalPenalty = violations.sumOf { weights[it.severity] ?: 0 }
        return (100 - totalPenalty).coerceIn(0, 100)
    }
}

/**
 * Represents a rule violation found during analysis
 */
data class RuleViolation(
    val ruleId: String,
    val ruleName: String,
    val workflowId: String,
    val workflowName: String,
    val severity: Severity,
    val type: ViolationType,
    val message: String,
    val details: String? = null,
    val field: String? = null,
    val expectedValue: Any? = null,
    val actualValue: Any? = null,
    val suggestion: String? = null,
    val documentation: String? = null,
    val lineNumber: Int? = null,
    val columnNumber: Int? = null
) {
    /**
     * Check if violation is auto-fixable
     */
    fun isAutoFixable(): Boolean = suggestion != null && type == ViolationType.INCORRECT_VALUE
}

/**
 * Severity levels for violations
 */
enum class Severity {
    CRITICAL,  // Workflow will fail or cause data loss
    HIGH,      // Workflow will likely fail or behave incorrectly
    MEDIUM,    // Workflow may fail or have performance issues
    LOW,       // Minor issues or best practice violations
    INFO       // Informational messages
}

/**
 * Types of violations
 */
enum class ViolationType {
    SYNTAX_ERROR,
    MISSING_FIELD,
    INCORRECT_VALUE,
    PERMISSION_ERROR,
    CIRCULAR_DEPENDENCY,
    PERFORMANCE_ISSUE,
    DEPRECATED_USAGE,
    SECURITY_ISSUE,
    LOGICAL_ERROR,
    CONFIGURATION_ERROR,
    BEST_PRACTICE,
    ACCESSIBILITY,
    COMPATIBILITY
}

/**
 * Represents a suggestion for improvement
 */
data class Suggestion(
    val type: SuggestionType,
    val title: String,
    val description: String,
    val impact: Impact,
    val effort: Effort,
    val code: String? = null,
    val example: String? = null,
    val benefits: List<String> = emptyList(),
    val risks: List<String> = emptyList()
)

/**
 * Types of suggestions
 */
enum class SuggestionType {
    PERFORMANCE,
    SIMPLIFICATION,
    SECURITY,
    MAINTAINABILITY,
    BEST_PRACTICE,
    MODERNIZATION,
    OPTIMIZATION
}

/**
 * Impact level of a suggestion
 */
enum class Impact {
    HIGH,
    MEDIUM,
    LOW
}

/**
 * Effort required to implement a suggestion
 */
enum class Effort {
    MINIMAL,
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH
}

/**
 * Represents a blocked state transition
 */
data class BlockedTransition(
    val fromState: String,
    val toState: String,
    val reason: String,
    val blockingRuleId: String,
    val blockingRuleName: String,
    val workflowId: String,
    val workflowName: String,
    val resolution: String? = null
)

/**
 * Performance metrics from analysis
 */
data class PerformanceMetrics(
    val analysisTimeMs: Long,
    val rulesEvaluated: Int,
    val workflowsAnalyzed: Int,
    val fieldsChecked: Int,
    val cacheHitRate: Double? = null,
    val memoryUsedMb: Long? = null
)

/**
 * Summary of analysis results
 */
data class AnalysisSummary(
    val totalViolations: Int,
    val violationsBySeverity: Map<Severity, Int>,
    val violationsByType: Map<ViolationType, Int>,
    val totalSuggestions: Int,
    val blockedTransitions: Int,
    val healthScore: Int,
    val recommendedActions: List<String> = emptyList()
)