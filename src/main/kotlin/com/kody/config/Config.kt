package com.kody.config

import com.typesafe.config.ConfigFactory

data class ServerConfig(
    val port: Int,
    val host: String,
    val secret: String,
    val expireTime: Int,
)

object AppConfig {
    private lateinit var serverConfig: ServerConfig

    fun init() {
        val config = ConfigFactory.load().getConfig("server")
        serverConfig = ServerConfig(
            port = config.getInt("port"),
            host = config.getString("host"),
            secret = config.getString("secret"),
            expireTime = config.getInt("expire_time")
        )
    }

    fun getServerConfig(): ServerConfig {
        if (!::serverConfig.isInitialized) {
            throw IllegalStateException("AppConfig is not initialized. Call AppConfig.init() in main() first.")
        }
        return serverConfig
    }
}


