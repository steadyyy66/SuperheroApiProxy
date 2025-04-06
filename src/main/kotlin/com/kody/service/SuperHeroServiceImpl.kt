package com.kody.service


import com.kody.WatchHeroDaemon
import com.kody.cache.Cache
import com.kody.client.SuperHeroClient
import com.kody.com.kody.constant.Constant
import com.kody.com.kody.utils.JsonUtils
import com.kody.daemon.ChannelBasedFlowManager
import com.kody.grpc.*
import io.prometheus.client.Counter
import io.prometheus.client.Summary
import kotlinx.coroutines.flow.Flow
import mu.KotlinLogging

class SuperHeroService(

) : SuperHeroServiceGrpcKt.SuperHeroServiceCoroutineImplBase() {

    private val logger = KotlinLogging.logger {}

    private val requestSuccessCounter = Counter.build()
        .name("grpc_search_hero_success_total")
        .help("Total successful gRPC searchHero requests.")
        .register()

    private val requestFailureCounter = Counter.build()
        .name("grpc_search_hero_failure_total")
        .help("Total failed gRPC searchHero requests.")
        .register()

    private val requestLatency = Summary.build()
        .name("grpc_search_hero_latency_seconds")
        .help("gRPC searchHero latency in seconds.")
        .register()

    init {
        WatchHeroDaemon.startPolling()

    }

    override suspend fun searchHero(request: SearchHeroRequest): SearchHeroResponse {
        val timer = requestLatency.startTimer()

        try {
            // check cache exist
            val cacheValue = Cache.get(request.name)
            if (cacheValue != "") {
                logger.info { "catch the cache,key is {${request.name}}" }
                val resp = JsonUtils.JsonToSearchHeroResponse(cacheValue)
                return resp;
            }

            logger.info { "didn't catch the cache,key is {${request.name}" }
            // cache missing,call the api
            val response = SuperHeroClient.searchHero(request.name)

            if (response.response != Constant.HERO_API_SUCCESS) {
                requestFailureCounter.inc()
                return SearchHeroResponse.newBuilder()
                    .setResponse("error")
                    .build()
            }

            logger.info { "write the cache: ${response.toString().replace("\n", "")}" }
            Cache.checkAndUpdate(request.name, response)
            return response
        } finally {
            requestSuccessCounter.inc()
            val elapsed = timer.observeDuration()
            val formatted = String.format("%.3f", elapsed)
            logger.info("searchHero request finished in $formatted seconds")

        }
    }

    override fun subscribeUpdates(request: SubscribeRequest): Flow<SubscribeResponse> {
        return ChannelBasedFlowManager.registerNewSubscriber()
    }


}