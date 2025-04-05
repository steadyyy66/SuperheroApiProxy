package com.kody.cache


import com.kody.com.kody.utils.DigestUtils
import com.kody.com.kody.utils.JsonUtils
import com.kody.config.AppConfig
import com.kody.grpc.SearchHeroResponse
import mu.KotlinLogging


import java.util.concurrent.ConcurrentHashMap
import java.time.Instant

data class CacheEntry(
    val value: String,
    val timestamp: Long,
    val hash: String,
)

object Cache {

    private val logger = KotlinLogging.logger {}

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    fun get(searchTerm: String): String {
        val entry = cache[searchTerm] ?: return ""

        if (Instant.now().epochSecond - entry.timestamp > 0) {
            removeExpireCache(searchTerm)
            return ""
        }
        return entry.value
    }

    fun getKeyList(): List<String> {
        return cache.keys.toList()
    }

    fun removeExpireCache(name: String) {
        cache.remove(name)
    }

    fun getCacheEntry(searchTerm: String): CacheEntry? {
        val entry = cache[searchTerm]
        return entry
    }

    fun checkAndUpdate(searchTerm: String, response: SearchHeroResponse) {

        val value = JsonUtils.SearchHeroResponseToJson(response)

        // 2. 计算 MD5
        val md5Hash = DigestUtils.Md5(value)

        cache[searchTerm] = CacheEntry(
            value = value,
            timestamp = Instant.now().epochSecond + AppConfig.getServerConfig().expireTime,
            hash = md5Hash
        )
    }

}

