package com.youtrack.analyzer.infrastructure.cache

import io.lettuce.core.ClientOptions
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.TimeoutOptions
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.resource.ClientResources
import io.lettuce.core.resource.DefaultClientResources
import org.slf4j.LoggerFactory
import java.time.Duration

/**
 * Redis configuration and connection management
 */
class RedisConfig(
    val host: String = "localhost",
    val port: Int = 6379,
    val password: String? = null,
    val database: Int = 0,
    val connectionTimeout: Duration = Duration.ofSeconds(2),
    val commandTimeout: Duration = Duration.ofSeconds(2),
    val maxConnections: Int = 10
) {
    private val logger = LoggerFactory.getLogger(RedisConfig::class.java)

    private var client: RedisClient? = null
    private var connection: StatefulRedisConnection<String, String>? = null
    private var clientResources: ClientResources? = null

    /**
     * Create and configure Redis client
     */
    fun createClient(): RedisClient {
        if (client != null) {
            return client!!
        }

        logger.info("Creating Redis client for $host:$port")

        // Build Redis URI
        val uriBuilder = RedisURI.builder()
            .withHost(host)
            .withPort(port)
            .withDatabase(database)
            .withTimeout(commandTimeout)

        password?.let {
            uriBuilder.withPassword(it.toCharArray())
        }

        val uri = uriBuilder.build()

        // Create client resources with connection pooling
        clientResources = DefaultClientResources.builder()
            .ioThreadPoolSize(4)
            .computationThreadPoolSize(4)
            .build()

        // Create Redis client
        client = RedisClient.create(clientResources, uri)

        // Configure client options
        val clientOptions = ClientOptions.builder()
            .autoReconnect(true)
            .pingBeforeActivateConnection(true)
            .cancelCommandsOnReconnectFailure(false)
            .suspendReconnectOnProtocolFailure(false)
            .requestQueueSize(1000)
            .disconnectedBehavior(ClientOptions.DisconnectedBehavior.ACCEPT_COMMANDS)
            .timeoutOptions(
                TimeoutOptions.builder()
                    .fixedTimeout(commandTimeout)
                    .build()
            )
            .build()

        client?.options = clientOptions

        logger.info("Redis client created successfully")
        return client!!
    }

    /**
     * Get Redis connection
     */
    fun getConnection(): StatefulRedisConnection<String, String> {
        if (connection == null || !connection!!.isOpen) {
            val redisClient = client ?: createClient()
            connection = redisClient.connect()
            logger.info("Redis connection established")
        }
        return connection!!
    }

    /**
     * Get coroutine-based Redis commands
     */
    fun getCoroutineCommands(): RedisCoroutinesCommands<String, String> {
        return getConnection().coroutines()
    }

    /**
     * Get async Redis commands
     */
    fun getAsyncCommands(): RedisAsyncCommands<String, String> {
        return getConnection().async()
    }

    /**
     * Test Redis connection
     */
    suspend fun testConnection(): Boolean {
        return try {
            val commands = getCoroutineCommands()
            val response = commands.ping()
            logger.info("Redis ping response: $response")
            response == "PONG"
        } catch (e: Exception) {
            logger.error("Failed to ping Redis", e)
            false
        }
    }

    /**
     * Get Redis server info
     */
    suspend fun getInfo(): Map<String, String> {
        return try {
            val commands = getCoroutineCommands()
            val info = commands.info()

            // Parse info string into map
            info?.split("\r\n", "\n")
                ?.filter { it.contains(":") && !it.startsWith("#") }
                ?.associate {
                    val parts = it.split(":", limit = 2)
                    parts[0] to parts.getOrElse(1) { "" }
                } ?: emptyMap()
        } catch (e: Exception) {
            logger.error("Failed to get Redis info", e)
            emptyMap()
        }
    }

    /**
     * Close Redis connection and client
     */
    fun close() {
        try {
            connection?.close()
            client?.shutdown()
            clientResources?.shutdown()
            logger.info("Redis connections closed")
        } catch (e: Exception) {
            logger.error("Error closing Redis connections", e)
        }
    }

    companion object {
        /**
         * Create RedisConfig from environment variables
         */
        fun fromEnvironment(): RedisConfig {
            return RedisConfig(
                host = System.getenv("REDIS_HOST") ?: "localhost",
                port = System.getenv("REDIS_PORT")?.toIntOrNull() ?: 6379,
                password = System.getenv("REDIS_PASSWORD"),
                database = System.getenv("REDIS_DATABASE")?.toIntOrNull() ?: 0
            )
        }

        /**
         * Create RedisConfig for local development
         */
        fun local(): RedisConfig {
            return RedisConfig(
                host = "localhost",
                port = 6379
            )
        }
    }
}