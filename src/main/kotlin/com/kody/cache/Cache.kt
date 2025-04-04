package com.kody.cache


import com.kody.config.AppConfig
import com.kody.grpc.Hero
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule


import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.time.Instant

private data class CacheEntry(
    val heroes: String,
    val timestamp: Long,
    val hash: String,
)

object Cache {
    private val cache = ConcurrentHashMap<String, CacheEntry>()

    fun get(searchTerm: String): String {
        val entry = cache[searchTerm] ?: return ""

        if (Instant.now().epochSecond - entry.timestamp > AppConfig.getServerConfig().expireTime) {
            cache.remove(searchTerm)
            return ""
        }
        return entry.heroes
    }

    fun checkAndUpdate(searchTerm: String, heroes: List<Hero>) {



        val mapper: ObjectMapper = jacksonObjectMapper().registerKotlinModule()
        val jsonString = mapper.writeValueAsString(heroes)

        // 2. 计算 MD5
        val md5Hash = md5(jsonString)

        cache[searchTerm] = CacheEntry(
            heroes = heroes,
            timestamp = Instant.now().epochSecond,
            hash = md5Hash
        )
    }

    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

}

