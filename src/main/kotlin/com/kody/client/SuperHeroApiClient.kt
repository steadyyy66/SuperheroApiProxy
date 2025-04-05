package com.kody.client

import mu.KotlinLogging
import com.kody.com.kody.constant.Constant
import com.kody.config.AppConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import com.kody.grpc.SearchHeroResponse
import okhttp3.Response

import com.google.protobuf.util.JsonFormat
import com.kody.com.kody.utils.DigestUtils

object SuperHeroClient {
    private val client = OkHttpClient()

    private val logger = KotlinLogging.logger {}

    fun searchHero(name: String): SearchHeroResponse {

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
        }
    }


    fun getAccessToken(): String {
        val algorithm = "AES/CBC/PKCS5Padding"
        val key = "1234567890abcdef" // 16字符密钥 (128-bit)
        val iv = "abcdef1234567890" // 16字符 IV

        return DigestUtils.Decrypte(
            algorithm,
            key,
            iv,
            AppConfig.getServerConfig().secret
        )

    }
}

