package com.youtrack.analyzer.infrastructure.cache

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Simplified Redis-based implementation of CacheManager using Lettuce client.
 *
 * This implementation focuses on core caching functionality without production-grade
 * complexity like mutex locking, statistics tracking, or scan-based pattern matching.
 */
class RedisCacheManager(
    private val redisConfig: RedisConfig,
    private val keyPrefix: String = "youtrack:analyzer:",
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
) : CacheManager {

    private val logger = LoggerFactory.getLogger(RedisCacheManager::class.java)

    /**
     * Get or compute value from cache.
     * Redis operations are atomic, so no mutex locking is needed.
     */
    override suspend fun <T> get(
        key: String,
        ttl: Duration,
        loader: suspend () -> T
    ): T {
        val fullKey = prefixKey(key)

        // Try to get from cache first
        val cachedString = getFromRedis(fullKey)
        if (cachedString != null) {
            logger.trace("Cache hit for key: $key")
            // Note: Caller must handle deserialization since we can't infer type T
            // This is a simplified approach - in production, use inline reified
            @Suppress("UNCHECKED_CAST")
            return cachedString as T
        }

        // Cache miss - load value
        logger.trace("Cache miss for key: $key, loading...")

        val value = loader()
        // Store as-is - callers should serialize before caching
        putToRedis(fullKey, value.toString(), ttl)
        return value
    }

    /**
     * Get value from cache if present
     */
    override suspend fun <T> getIfPresent(key: String): T? {
        val fullKey = prefixKey(key)
        val cachedString = getFromRedis(fullKey)

        return if (cachedString != null) {
            logger.trace("Cache hit for key: $key")
            @Suppress("UNCHECKED_CAST")
            cachedString as T
        } else {
            logger.trace("Cache miss for key: $key")
            null
        }
    }

    /**
     * Put value in cache with TTL
     */
    override suspend fun <T> put(key: String, value: T, ttl: Duration) {
        val fullKey = prefixKey(key)
        // Store as string - callers should serialize before caching
        val stringValue = when (value) {
            is String -> value
            else -> value.toString()
        }
        putToRedis(fullKey, stringValue, ttl)
        logger.trace("Cached value for key: $key with TTL: ${ttl.inWholeSeconds}s")
    }

    /**
     * Check if key exists
     */
    override suspend fun exists(key: String): Boolean {
        return try {
            val commands = redisConfig.getCoroutineCommands()
            val exists = commands.exists(prefixKey(key))
            (exists ?: 0L) > 0
        } catch (e: Exception) {
            logger.error("Failed to check key existence: $key", e)
            false
        }
    }

    /**
     * Invalidate a specific key
     */
    override suspend fun invalidate(key: String) {
        try {
            val commands = redisConfig.getCoroutineCommands()
            val deleted = commands.del(prefixKey(key))
            if ((deleted ?: 0L) > 0) {
                logger.debug("Invalidated cache key: $key")
            }
        } catch (e: Exception) {
            logger.error("Failed to invalidate key: $key", e)
        }
    }

    /**
     * Invalidate multiple keys
     */
    override suspend fun invalidateAll(keys: List<String>) {
        if (keys.isEmpty()) return

        try {
            val commands = redisConfig.getCoroutineCommands()
            val prefixedKeys = keys.map { prefixKey(it) }.toTypedArray()
            val deleted = commands.del(*prefixedKeys)
            if ((deleted ?: 0L) > 0) {
                logger.debug("Invalidated ${deleted} cache keys")
            }
        } catch (e: Exception) {
            logger.error("Failed to invalidate keys", e)
        }
    }

    /**
     * Invalidate keys matching a pattern.
     * Uses simple KEYS command - suitable for small datasets (internship scope).
     * For production with large datasets, use SCAN instead.
     */
    override suspend fun invalidatePattern(pattern: String) {
        try {
            val commands = redisConfig.getCoroutineCommands()
            val fullPattern = prefixKey(pattern)

            // Simple approach: get all matching keys at once
            val matchedKeys = commands.keys(fullPattern)

            if (matchedKeys != null) {
                val keysList: List<String> = when (matchedKeys) {
                    is List<*> -> matchedKeys.filterIsInstance<String>()
                    else -> listOf(matchedKeys.toString())
                }
                if (keysList.isNotEmpty()) {
                    val deleted = commands.del(*keysList.toTypedArray())
                    logger.debug("Invalidated ${deleted ?: 0} cache keys matching pattern: $pattern")
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to invalidate pattern: $pattern", e)
        }
    }

    /**
     * Clear all cache entries
     */
    override suspend fun clear() {
        try {
            invalidatePattern("*")
            logger.info("Cleared all cache entries")
        } catch (e: Exception) {
            logger.error("Failed to clear cache", e)
        }
    }

    /**
     * Get cache statistics from Redis directly.
     * No local tracking needed - Redis provides this information.
     */
    override suspend fun getStats(): CacheStats {
        val commands = redisConfig.getCoroutineCommands()

        return try {
            val dbSize = commands.dbsize() ?: 0L
            val info = redisConfig.getInfo()
            val memoryUsed = info["used_memory"]?.toLongOrNull()

            CacheStats(
                hits = 0,  // Use Redis INFO stats if needed
                misses = 0,
                evictions = 0,
                size = dbSize,
                memoryUsed = memoryUsed
            )
        } catch (e: Exception) {
            logger.error("Failed to get cache stats", e)
            CacheStats(
                hits = 0,
                misses = 0,
                evictions = 0,
                size = 0
            )
        }
    }

    /**
     * Get remaining TTL for a key
     */
    override suspend fun getTtl(key: String): Duration? {
        return try {
            val commands = redisConfig.getCoroutineCommands()
            val ttlSeconds = commands.ttl(prefixKey(key))
            if (ttlSeconds != null && ttlSeconds > 0) {
                (ttlSeconds * 1000).milliseconds
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Failed to get TTL for key: $key", e)
            null
        }
    }

    /**
     * Get value from Redis
     */
    private suspend fun getFromRedis(key: String): String? {
        return try {
            val commands = redisConfig.getCoroutineCommands()
            commands.get(key)
        } catch (e: Exception) {
            logger.error("Failed to get value from Redis for key: $key", e)
            null
        }
    }

    /**
     * Put value to Redis with TTL
     */
    private suspend fun putToRedis(key: String, value: String, ttl: Duration) {
        try {
            val commands = redisConfig.getCoroutineCommands()
            commands.setex(key, ttl.inWholeSeconds, value)
        } catch (e: Exception) {
            logger.error("Failed to put value to Redis for key: $key", e)
        }
    }

    /**
     * Prefix key with namespace
     */
    private fun prefixKey(key: String): String {
        return if (key.startsWith(keyPrefix)) {
            key
        } else {
            keyPrefix + key
        }
    }

    companion object {
        /**
         * Cache key generators for common entities
         */
        object CacheKeys {
            fun workflow(projectId: String) = "workflow:$projectId"
            fun workflowRules(workflowId: String) = "workflow:rules:$workflowId"
            fun issue(issueId: String) = "issue:$issueId"
            fun project(projectId: String) = "project:$projectId"
            fun activities(issueId: String, cursor: String? = null) =
                "activities:$issueId${cursor?.let { ":$it" } ?: ""}"
            fun projectIssues(projectId: String, query: String) =
                "project:$projectId:issues:${query.hashCode()}"
        }
    }
}
