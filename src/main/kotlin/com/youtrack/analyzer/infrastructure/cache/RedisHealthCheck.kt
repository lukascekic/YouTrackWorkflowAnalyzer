package com.youtrack.analyzer.infrastructure.cache

import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Health check for Redis connection
 */
class RedisHealthCheck(
    private val redisConfig: RedisConfig,
    private val timeout: Duration = 5.seconds
) {
    private val logger = LoggerFactory.getLogger(RedisHealthCheck::class.java)

    data class HealthStatus(
        val healthy: Boolean,
        val message: String,
        val details: Map<String, Any> = emptyMap()
    )

    /**
     * Check Redis health
     */
    suspend fun check(): HealthStatus {
        return try {
            withTimeout(timeout) {
                performHealthCheck()
            }
        } catch (e: Exception) {
            logger.error("Redis health check failed", e)
            HealthStatus(
                healthy = false,
                message = "Redis health check failed: ${e.message}",
                details = mapOf(
                    "error" to (e.message ?: "Unknown error"),
                    "errorType" to e::class.simpleName!!
                )
            )
        }
    }

    private suspend fun performHealthCheck(): HealthStatus {
        // Test connection
        val connected = redisConfig.testConnection()
        if (!connected) {
            return HealthStatus(
                healthy = false,
                message = "Cannot connect to Redis",
                details = mapOf(
                    "host" to redisConfig.host,
                    "port" to redisConfig.port
                )
            )
        }

        // Get server info
        val info = redisConfig.getInfo()
        if (info.isEmpty()) {
            return HealthStatus(
                healthy = false,
                message = "Cannot retrieve Redis server info"
            )
        }

        // Extract key metrics
        val version = info["redis_version"] ?: "unknown"
        val role = info["role"] ?: "unknown"
        val connectedClients = info["connected_clients"]?.toIntOrNull() ?: 0
        val usedMemory = info["used_memory_human"] ?: "unknown"
        val uptimeSeconds = info["uptime_in_seconds"]?.toLongOrNull() ?: 0

        // Check memory usage
        val usedMemoryBytes = info["used_memory"]?.toLongOrNull() ?: 0
        val maxMemoryBytes = info["maxmemory"]?.toLongOrNull() ?: 0
        val memoryUsagePercent = if (maxMemoryBytes > 0) {
            (usedMemoryBytes.toDouble() / maxMemoryBytes * 100).toInt()
        } else {
            0
        }

        // Determine health based on metrics
        val healthy = when {
            connectedClients > 500 -> false // Too many connections
            memoryUsagePercent > 90 -> false // Memory almost full
            else -> true
        }

        val message = when {
            !healthy && connectedClients > 500 -> "Too many Redis connections: $connectedClients"
            !healthy && memoryUsagePercent > 90 -> "Redis memory usage critical: $memoryUsagePercent%"
            else -> "Redis is healthy"
        }

        return HealthStatus(
            healthy = healthy,
            message = message,
            details = mapOf(
                "version" to version,
                "role" to role,
                "connectedClients" to connectedClients,
                "usedMemory" to usedMemory,
                "memoryUsagePercent" to memoryUsagePercent,
                "uptimeSeconds" to uptimeSeconds,
                "host" to redisConfig.host,
                "port" to redisConfig.port
            )
        )
    }

    /**
     * Quick health check - just test connectivity
     */
    suspend fun quickCheck(): Boolean {
        return try {
            withTimeout(2.seconds) {
                redisConfig.testConnection()
            }
        } catch (e: Exception) {
            logger.debug("Redis quick check failed", e)
            false
        }
    }
}