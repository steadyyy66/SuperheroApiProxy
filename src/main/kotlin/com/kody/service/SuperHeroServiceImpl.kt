package com.kody.service


import com.kody.UpdatePoller
import com.kody.cache.Cache
import com.kody.client.SuperHeroClient
import com.kody.com.kody.constant.Constant
import com.kody.com.kody.utils.JsonUtils

import com.kody.grpc.SuperHeroServiceGrpcKt
import com.kody.grpc.SearchHeroRequest
import com.kody.grpc.SearchHeroResponse
import mu.KotlinLogging

class SuperHeroService(

) : SuperHeroServiceGrpcKt.SuperHeroServiceCoroutineImplBase() {
    private val logger = KotlinLogging.logger {}

    init {
        UpdatePoller.startPolling()
    }

    override suspend fun searchHero(request: SearchHeroRequest): SearchHeroResponse {
        // check cache exist
        val cacheValue = Cache.get(request.name)
        if (cacheValue != "") {
            logger.info { "catch the cache,key is {${request.name}}" }
            val resp = JsonUtils.JsonToSearchHeroResponse(cacheValue)
            return resp;
        }

        logger.info { "didn't catch the cache,key is {${request.name}" }
        // 缓存未命中，调用API
        val response = SuperHeroClient.searchHero(request.name)

        if (response.response != Constant.HERO_API_SUCCESS) {
            return SearchHeroResponse.newBuilder()
                .setResponse("error")
                .build()
        }

        logger.info { "write the cache: ${response}" }
        Cache.checkAndUpdate(request.name, response)

        return response
    }
}