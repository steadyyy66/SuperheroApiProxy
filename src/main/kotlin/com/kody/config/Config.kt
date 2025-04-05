package com.kody.config

import com.typesafe.config.ConfigFactory

data class ServerConfig(
    val grpcPort: Int,
    val grpcHost: String,
    val secret: String,
    val expireTime: Int,
    val intervalMillis: Long,
    val apiWebsite: String,
    val prometheusPort: Int
)

object AppConfig {
    private lateinit var serverConfig: ServerConfig

    fun init() {
        val config = ConfigFactory.load().getConfig("server")
        serverConfig = ServerConfig(
            grpcPort = config.getInt("grpc_port"),
            grpcHost = config.getString("grpc_host"),
            secret = config.getString("secret"),
            expireTime = config.getInt("expire_time"),
            intervalMillis = config.getLong("interval_millis"),
            apiWebsite = config.getString("api_website"),
            prometheusPort = config.getInt("prometheus_port"),
        )
    }

    fun getServerConfig(): ServerConfig {
        if (!::serverConfig.isInitialized) {
            throw IllegalStateException("AppConfig is not initialized. Call AppConfig.init() in main() first.")
        }
        return serverConfig
    }
}


