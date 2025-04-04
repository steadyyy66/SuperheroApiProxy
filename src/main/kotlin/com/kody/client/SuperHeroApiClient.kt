package com.kody.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import com.kody.com.kody.constant.Constant
import com.kody.config.AppConfig

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import com.kody.grpc.SearchHeroResponse
import okhttp3.Response
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.protobuf.util.JsonFormat

object SuperHeroClient {
    private val client = OkHttpClient()

    private val logger = KotlinLogging.logger {}

    fun searchHero(name: String): SearchHeroResponse {

        val accessToken = getAccessToken();
        val request = Request.Builder()
            .url("${Constant.SUPER_HERO_API_URL}/$accessToken/search/$name")
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
            logger.error {"Error during API call: ${e.message}"}
            throw e  // 或者 return null / Result.failure(...) 看你的需求
        } finally {
            response?.close()
        }
    }

    fun jsonToProto(json: String): SearchHeroResponse {
        val builder = SearchHeroResponse.newBuilder()
        JsonFormat.parser().ignoringUnknownFields().merge(json, builder)
        return builder.build()
    }

    fun getAccessToken(): String {
        val algorithm = "AES/CBC/PKCS5Padding"
        val key = "1234567890abcdef" // 16字符密钥 (128-bit)
        val iv = "abcdef1234567890" // 16字符 IV

        val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
        val ivParameterSpec = IvParameterSpec(iv.toByteArray())


        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val decodedBytes = Base64.getDecoder().decode(AppConfig.getServerConfig().secret)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, Charsets.UTF_8)

    }
}

data class SuperHeroResponse1(
    val response: String,
    val results: List<Hero>?
)

data class Hero(
    val id: String,
    val name: String,
    val powerstats: PowerStats,
    val biography: Biography,
    val appearance: Appearance
)

data class PowerStats(
    val intelligence: String,
    val strength: String,
    val speed: String,
    val durability: String,
    val power: String,
    val combat: String
)

data class Biography(
    val fullName: String,
    val alterEgos: String,
    val aliases: List<String>,
    val placeOfBirth: String,
    val firstAppearance: String,
    val publisher: String,
    val alignment: String
)

data class Appearance(
    val gender: String,
    val race: String,
    val height: List<String>,
    val weight: List<String>,
    val eyeColor: String,
    val hairColor: String
)