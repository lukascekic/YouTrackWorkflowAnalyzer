package com.youtrack.analyzer.infrastructure.config

data class AppConfig(
    val server: ServerConfig,
    val youtrack: YouTrackConfig,
    val ai: AIConfig,
    val cache: CacheConfig,
    val logging: LoggingConfig
)

data class ServerConfig(
    val port: Int,
    val host: String
)

data class YouTrackConfig(
    val baseUrl: String,
    val token: String,
    val timeout: Long
)

data class AIConfig(
    val provider: String,
    val model: String,
    val apiKey: String,
    val maxTokens: Int,
    val temperature: Double
)

data class CacheConfig(
    val enabled: Boolean,
    val ttl: Long,
    val maxSize: Int
)

data class LoggingConfig(
    val level: String,
    val pattern: String
)
