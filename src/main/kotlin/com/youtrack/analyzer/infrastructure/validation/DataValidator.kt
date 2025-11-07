package com.youtrack.analyzer.infrastructure.validation

import com.youtrack.analyzer.domain.model.*
import org.slf4j.LoggerFactory

/**
 * Data validation utilities for YouTrack entities
 */
object DataValidator {
    private val logger = LoggerFactory.getLogger(DataValidator::class.java)

    // Regex patterns for validation
    private val ISSUE_ID_PATTERN = Regex("^[A-Z]+-\\d+$")
    private val PROJECT_ID_PATTERN = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$|^\\w+$")
    private val URL_PATTERN = Regex("^https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]+$")
    private val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")

    /**
     * Validate issue data
     */
    fun validateIssue(issue: Issue): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Validate ID format
        if (!isValidIssueId(issue.id)) {
            errors.add(ValidationError("id", "Invalid issue ID format: ${issue.id}"))
        }

        // Validate project ID
        if (issue.projectId.isBlank()) {
            errors.add(ValidationError("projectId", "Project ID cannot be empty"))
        }

        // Validate summary
        if (issue.summary.isBlank()) {
            errors.add(ValidationError("summary", "Issue summary cannot be empty"))
        } else if (issue.summary.length > 255) {
            errors.add(ValidationError("summary", "Issue summary too long (max 255 characters)"))
        }

        // Validate state
        if (issue.state.isBlank()) {
            errors.add(ValidationError("state", "Issue state cannot be empty"))
        }

        // Validate timestamps
        if (issue.created > issue.updated) {
            errors.add(ValidationError("timestamps", "Created date cannot be after updated date"))
        }

        issue.resolved?.let { resolved ->
            if (resolved < issue.created) {
                errors.add(ValidationError("resolved", "Resolved date cannot be before created date"))
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Validate workflow data
     */
    fun validateWorkflow(workflow: Workflow): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Validate ID
        if (workflow.id.isBlank()) {
            errors.add(ValidationError("id", "Workflow ID cannot be empty"))
        }

        // Validate name
        if (workflow.name.isBlank()) {
            errors.add(ValidationError("name", "Workflow name cannot be empty"))
        } else if (workflow.name.length > 100) {
            errors.add(ValidationError("name", "Workflow name too long (max 100 characters)"))
        }

        // Validate rules
        workflow.rules.forEachIndexed { index, rule ->
            val ruleErrors = validateWorkflowRule(rule)
            if (!ruleErrors.isValid) {
                errors.addAll(ruleErrors.errors.map { error ->
                    ValidationError("rules[$index].${error.field}", error.message)
                })
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Validate workflow rule
     */
    fun validateWorkflowRule(rule: WorkflowRule): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Validate ID
        if (rule.id.isBlank()) {
            errors.add(ValidationError("id", "Rule ID cannot be empty"))
        }

        // Validate name
        if (rule.name.isBlank()) {
            errors.add(ValidationError("name", "Rule name cannot be empty"))
        }

        // Validate guard expression if present
        rule.guard?.let { guard ->
            if (guard.length > 5000) {
                errors.add(ValidationError("guard", "Guard expression too long (max 5000 characters)"))
            }
            // Could add more complex validation of the guard syntax here
        }

        // Validate action if present
        rule.action?.let { action ->
            if (action.length > 10000) {
                errors.add(ValidationError("action", "Action script too long (max 10000 characters)"))
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Validate project data
     */
    fun validateProject(project: Project): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Validate ID
        if (project.id.isBlank()) {
            errors.add(ValidationError("id", "Project ID cannot be empty"))
        }

        // Validate short name
        if (project.shortName.isBlank()) {
            errors.add(ValidationError("shortName", "Project short name cannot be empty"))
        } else if (!project.shortName.matches(Regex("^[A-Z][A-Z0-9]*$"))) {
            errors.add(ValidationError("shortName", "Project short name must start with a letter and contain only uppercase letters and numbers"))
        }

        // Validate name
        if (project.name.isBlank()) {
            errors.add(ValidationError("name", "Project name cannot be empty"))
        } else if (project.name.length > 100) {
            errors.add(ValidationError("name", "Project name too long (max 100 characters)"))
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Validate API response
     */
    fun validateApiResponse(response: Any?): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (response == null) {
            errors.add(ValidationError("response", "API response is null"))
            return ValidationResult(false, errors)
        }

        // Add more specific validation based on response type
        when (response) {
            is List<*> -> {
                if (response.isEmpty()) {
                    logger.debug("Empty list response")
                }
            }
            is Map<*, *> -> {
                if (response.isEmpty()) {
                    logger.debug("Empty map response")
                }
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Check if issue ID format is valid
     */
    fun isValidIssueId(id: String): Boolean {
        return id.isNotBlank() && (ISSUE_ID_PATTERN.matches(id) || PROJECT_ID_PATTERN.matches(id))
    }

    /**
     * Check if project ID format is valid
     */
    fun isValidProjectId(id: String): Boolean {
        return id.isNotBlank() && PROJECT_ID_PATTERN.matches(id)
    }

    /**
     * Check if URL format is valid
     */
    fun isValidUrl(url: String): Boolean {
        return URL_PATTERN.matches(url)
    }

    /**
     * Check if email format is valid
     */
    fun isValidEmail(email: String): Boolean {
        return EMAIL_PATTERN.matches(email)
    }

    /**
     * Sanitize input string to prevent injection attacks
     */
    fun sanitizeInput(input: String): String {
        return input
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
            .replace("&", "&amp;")
            .trim()
    }

    /**
     * Validate and sanitize search query
     */
    fun validateSearchQuery(query: String): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (query.isBlank()) {
            errors.add(ValidationError("query", "Search query cannot be empty"))
        } else if (query.length > 1000) {
            errors.add(ValidationError("query", "Search query too long (max 1000 characters)"))
        }

        // Check for potential injection patterns
        val dangerousPatterns = listOf(
            "';",
            "--",
            "/*",
            "*/",
            "xp_",
            "sp_",
            "exec",
            "execute",
            "drop",
            "delete",
            "insert",
            "update"
        )

        val lowerQuery = query.lowercase()
        for (pattern in dangerousPatterns) {
            if (lowerQuery.contains(pattern)) {
                errors.add(ValidationError("query", "Query contains potentially dangerous pattern: $pattern"))
                break
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Validate pagination parameters
     */
    fun validatePagination(limit: Int, offset: Int): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (limit < 1) {
            errors.add(ValidationError("limit", "Limit must be at least 1"))
        } else if (limit > 1000) {
            errors.add(ValidationError("limit", "Limit cannot exceed 1000"))
        }

        if (offset < 0) {
            errors.add(ValidationError("offset", "Offset cannot be negative"))
        } else if (offset > 100000) {
            errors.add(ValidationError("offset", "Offset too large (max 100000)"))
        }

        return ValidationResult(errors.isEmpty(), errors)
    }
}

/**
 * Result of validation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList()
) {
    fun requireValid() {
        if (!isValid) {
            throw ValidationException(errors)
        }
    }
}

/**
 * Validation error
 */
data class ValidationError(
    val field: String,
    val message: String
)

/**
 * Exception thrown when validation fails
 */
class ValidationException(
    val errors: List<ValidationError>
) : RuntimeException("Validation failed: ${errors.joinToString(", ") { "${it.field}: ${it.message}" }}")