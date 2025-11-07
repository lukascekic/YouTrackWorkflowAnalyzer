package com.youtrack.analyzer.infrastructure.cache

import kotlin.time.Duration

/**
 * Interface for cache management operations
 */
interface CacheManager {
    /**
     * Get a value from cache or load it if not present
     */
    suspend fun <T> get(
        key: String,
        ttl: Duration,
        loader: suspend () -> T
    ): T

    /**
     * Get a value from cache without loading
     */
    suspend fun <T> getIfPresent(key: String): T?

    /**
     * Put a value in cache
     */
    suspend fun <T> put(key: String, value: T, ttl: Duration)

    /**
     * Check if a key exists in cache
     */
    suspend fun exists(key: String): Boolean

    /**
     * Invalidate a specific cache entry
     */
    suspend fun invalidate(key: String)

    /**
     * Invalidate multiple cache entries
     */
    suspend fun invalidateAll(keys: List<String>)

    /**
     * Invalidate cache entries matching a pattern
     */
    suspend fun invalidatePattern(pattern: String)

    /**
     * Clear all cache entries
     */
    suspend fun clear()

    /**
     * Get cache statistics
     */
    suspend fun getStats(): CacheStats

    /**
     * Get remaining TTL for a key
     */
    suspend fun getTtl(key: String): Duration?
}

/**
 * Cache statistics
 */
data class CacheStats(
    val hits: Long,
    val misses: Long,
    val evictions: Long,
    val size: Long,
    val hitRate: Double = if (hits + misses > 0) hits.toDouble() / (hits + misses) else 0.0,
    val memoryUsed: Long? = null
)