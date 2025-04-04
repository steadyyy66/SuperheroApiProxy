package com.kody.cache


import com.kody.com.kody.utils.DigestUtils
import com.kody.com.kody.utils.JsonUtils
import com.kody.config.AppConfig
import com.kody.grpc.SearchHeroResponse


import java.util.concurrent.ConcurrentHashMap
import java.time.Instant

private data class CacheEntry(
    val value: String,
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
        return entry.value
    }

    fun checkAndUpdate(searchTerm: String, response: SearchHeroResponse) {

        val value = JsonUtils.SearchHeroResponseToJson(response)

        // 2. 计算 MD5
        val md5Hash = DigestUtils.Md5(value)

        cache[searchTerm] = CacheEntry(
            value = value,
            timestamp = Instant.now().epochSecond,
            hash = md5Hash
        )
    }

}

