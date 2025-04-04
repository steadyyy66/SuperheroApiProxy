package com.kody.service


import com.kody.cache.Cache
import com.kody.client.SuperHeroClient
import com.kody.com.kody.constant.Constant
import com.kody.com.kody.utils.JsonUtils

import com.kody.grpc.SuperHeroServiceGrpcKt
import com.kody.grpc.SearchHeroRequest
import com.kody.grpc.SearchHeroResponse

class SuperHeroService(

) : SuperHeroServiceGrpcKt.SuperHeroServiceCoroutineImplBase() {

    override suspend fun searchHero(request: SearchHeroRequest): SearchHeroResponse {
        // 先检查缓存
        val cacheValue = Cache.get(request.name)
        if (cacheValue != "") {
            log.
            val resp = JsonUtils.JsonToSearchHeroResponse(cacheValue)
            return resp;
        }

        // 缓存未命中，调用API
        val response = SuperHeroClient.searchHero(request.name)

        if (response.response != Constant.HERO_API_SUCCESS) {
            return SearchHeroResponse.newBuilder()
                .setResponse("error")
                .build()
        }
        val heroes = response.resultsList
        if (heroes != null) {
            Cache.checkAndUpdate(request.name, heroes)
        }

        return response
    }
}