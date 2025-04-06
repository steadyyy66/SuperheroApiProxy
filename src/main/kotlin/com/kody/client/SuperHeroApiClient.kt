package com.kody.client

import mu.KotlinLogging
import com.kody.com.kody.constant.Constant
import com.kody.config.AppConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import com.kody.grpc.SearchHeroResponse
import okhttp3.Response

import com.google.protobuf.util.JsonFormat
import com.kody.com.kody.utils.EncryptionUtils
import io.prometheus.client.Summary

object SuperHeroClient {
    private val client = OkHttpClient()

    private val logger = KotlinLogging.logger {}

    private val httpLatency = Summary.build()
        .name("external_api_latency_seconds")
        .help("Latency for SuperHero API requests.")
        .register()

    fun searchHero(name: String): SearchHeroResponse {
        val timer = httpLatency.startTimer()

        val accessToken = getAccessToken();
        val request = Request.Builder()
            .url("${AppConfig.getServerConfig().apiWebsite}/$accessToken/search/$name")
            .build()

        logger.debug { "begin to call api: ${request.url}" }
        val call = client.newCall(request)
        var response: Response? = null

        try {
            response = call.execute()

            if (!response.isSuccessful) {
                throw RuntimeException("API call failed: ${response.code}")
            }
            val body = response.body?.string() ?: throw RuntimeException("Empty response")

            logger.debug { "response from api: ${body.toString()}" }

            val builder = SearchHeroResponse.newBuilder()
            JsonFormat.parser().ignoringUnknownFields().merge(body.toString(), builder)
            return builder.build()

        } catch (e: Exception) {
            logger.error { "Error during API call: ${e.message}" }
            throw e  // 或者 return null / Result.failure(...) 看你的需求
        } finally {
            response?.close()
            val elapsed = timer.observeDuration()
            val formatted = String.format("%.3f", elapsed)
            logger.info("call searchHero api finished in $formatted seconds")
        }
    }


    fun getAccessToken(): String {
        val algorithm = "AES/CBC/PKCS5Padding"
        val key = "1234567890abcdef" // 16字符密钥 (128-bit)
        val iv = "abcdef1234567890" // 16字符 IV

        return EncryptionUtils.Decrypte(
            algorithm,
            key,
            iv,
            AppConfig.getServerConfig().secret
        )

    }
}

