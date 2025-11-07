package com.youtrack.analyzer.infrastructure.youtrack

import kotlinx.coroutines.delay

suspend fun <T> simpleRetry(
    maxAttempts: Int = 3,
    delayMs: Long = 1000,
    operation: suspend () -> T
): T {
    repeat(maxAttempts) { attempt ->
        try {
            return operation()
        } catch (e: Exception) {
            if (attempt < maxAttempts - 1) {
                delay(delayMs)
            } else {
                throw e
            }
        }
    }
    error("Unreachable: retry loop should always return or throw")
}
