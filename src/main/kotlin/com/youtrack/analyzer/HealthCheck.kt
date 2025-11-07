package com.youtrack.analyzer

import com.youtrack.analyzer.infrastructure.cache.RedisConfig
import com.youtrack.analyzer.infrastructure.cache.RedisHealthCheck
import com.youtrack.analyzer.infrastructure.cache.RedisCacheManager
import com.youtrack.analyzer.infrastructure.youtrack.YouTrackClient
import com.youtrack.analyzer.infrastructure.youtrack.YouTrackClientConfig
import com.youtrack.analyzer.infrastructure.youtrack.YouTrackApiService
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds

/**
 * Health check utility to verify all infrastructure components
 */
fun main() = runBlocking {
    println("=".repeat(60))
    println("YouTrack Workflow Analyzer - Health Check")
    println("=".repeat(60))
    println()

    var allHealthy = true

    // 1. Check Redis connection
    println("1. Testing Redis Connection...")
    println("-".repeat(60))
    try {
        val redisConfig = RedisConfig.fromEnvironment()
        val redisHealth = RedisHealthCheck(redisConfig)

        val healthStatus = redisHealth.check()
        if (healthStatus.healthy) {
            println("✓ Redis is healthy")
            println("  Details:")
            healthStatus.details.forEach { (key, value) ->
                println("    - $key: $value")
            }
        } else {
            println("✗ Redis is unhealthy: ${healthStatus.message}")
            allHealthy = false
        }

        // Test cache operations
        println("\n2. Testing Redis Cache Operations...")
        println("-".repeat(60))
        val cacheManager = RedisCacheManager(redisConfig)

        // Test put/get
        cacheManager.put("test:key", "test-value", 10.seconds)
        val retrieved = cacheManager.getIfPresent<String>("test:key")

        if (retrieved == "test-value") {
            println("✓ Cache put/get works correctly")
        } else {
            println("✗ Cache put/get failed")
            allHealthy = false
        }

        // Test TTL
        val ttl = cacheManager.getTtl("test:key")
        if (ttl != null) {
            println("✓ TTL working correctly: ${ttl.inWholeSeconds}s remaining")
        } else {
            println("✗ TTL check failed")
            allHealthy = false
        }

        // Clean up
        cacheManager.invalidate("test:key")
        println("✓ Cache cleanup successful")

        // Show cache stats
        val stats = cacheManager.getStats()
        println("\n  Cache Statistics:")
        println("    - Hits: ${stats.hits}")
        println("    - Misses: ${stats.misses}")
        println("    - Hit Rate: ${(stats.hitRate * 100).toInt()}%")
        println("    - Total Keys: ${stats.size}")

        redisConfig.close()
    } catch (e: Exception) {
        println("✗ Redis test failed: ${e.message}")
        allHealthy = false
    }

    println()

    // 3. Check YouTrack API (if credentials provided)
    println("3. Testing YouTrack API Connection...")
    println("-".repeat(60))
    try {
        val baseUrl = System.getenv("YOUTRACK_BASE_URL")
        val token = System.getenv("YOUTRACK_API_TOKEN")

        if (baseUrl.isNullOrBlank() || token.isNullOrBlank()) {
            println("⊘ YouTrack API test skipped (credentials not provided)")
            println("  Set YOUTRACK_BASE_URL and YOUTRACK_API_TOKEN to test API")
        } else {
            val clientConfig = YouTrackClientConfig(
                baseUrl = baseUrl,
                token = token,
                timeout = 30.seconds
            )

            val client = YouTrackClient(clientConfig)
            val apiService = YouTrackApiService(client)

            val connectionTest = apiService.testConnection()
            if (connectionTest.isSuccess) {
                println("✓ YouTrack API connection successful")
            } else {
                println("✗ YouTrack API connection failed: ${connectionTest.exceptionOrNull()?.message}")
                allHealthy = false
            }

            client.close()
        }
    } catch (e: Exception) {
        println("✗ YouTrack API test failed: ${e.message}")
        allHealthy = false
    }

    println()
    println("=".repeat(60))

    if (allHealthy) {
        println("✓ All health checks passed!")
        println("=".repeat(60))
        println("\nNext steps:")
        println("1. Start Redis: docker-compose up -d redis")
        println("2. Set environment variables:")
        println("   - YOUTRACK_BASE_URL=https://your-instance.myjetbrains.com/youtrack")
        println("   - YOUTRACK_API_TOKEN=your-api-token")
        println("3. Run this health check again to verify API connection")
    } else {
        println("✗ Some health checks failed")
        println("=".repeat(60))
        println("\nTroubleshooting:")
        println("1. Ensure Redis is running: docker-compose up -d redis")
        println("2. Check Redis logs: docker-compose logs redis")
        println("3. Verify YouTrack credentials are correct")
        println("4. Check network connectivity")
    }

    println()
}