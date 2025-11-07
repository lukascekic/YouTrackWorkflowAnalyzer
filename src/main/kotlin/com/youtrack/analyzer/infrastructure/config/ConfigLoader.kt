package com.youtrack.analyzer.infrastructure.config

import com.typesafe.config.ConfigFactory
import io.github.cdimascio.dotenv.dotenv

object ConfigLoader {
    fun load(): AppConfig {
        val dotenv = dotenv {
            ignoreIfMissing = true
        }

        val config = ConfigFactory.load()

        return AppConfig(
            server = ServerConfig(
                port = dotenv["APP_PORT"]?.toIntOrNull() ?: config.getInt("app.server.port"),
                host = config.getString("app.server.host")
            ),
            youtrack = YouTrackConfig(
                baseUrl = dotenv["YOUTRACK_URL"] ?: config.getString("app.youtrack.baseUrl"),
                token = dotenv["YOUTRACK_TOKEN"] ?: config.getString("app.youtrack.token"),
                timeout = config.getLong("app.youtrack.timeout")
            ),
            ai = AIConfig(
                provider = config.getString("app.ai.provider"),
                model = config.getString("app.ai.model"),
                apiKey = dotenv["OPENAI_API_KEY"] ?: config.getString("app.ai.apiKey"),
                maxTokens = config.getInt("app.ai.maxTokens"),
                temperature = config.getDouble("app.ai.temperature")
            ),
            cache = CacheConfig(
                enabled = config.getBoolean("app.cache.enabled"),
                ttl = config.getLong("app.cache.ttl"),
                maxSize = config.getInt("app.cache.maxSize")
            ),
            logging = LoggingConfig(
                level = dotenv["LOG_LEVEL"] ?: config.getString("app.logging.level"),
                pattern = config.getString("app.logging.pattern")
            )
        )
    }
}
