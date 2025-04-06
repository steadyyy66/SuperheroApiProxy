package com.kody

import com.kody.cache.Cache
import com.kody.client.SuperHeroClient.searchHero
import com.kody.com.kody.utils.EncryptionUtils
import com.kody.com.kody.utils.JsonUtils
import com.kody.config.AppConfig
import com.kody.daemon.ChannelBasedFlowManager


import kotlinx.coroutines.*
import mu.KotlinLogging
import java.time.Instant

object UpdatePoller {
    private val logger = KotlinLogging.logger {}
    fun startPolling() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            logger.info { "Starting watching" }

            while (true) {
                try {
                    val keywords = Cache.getKeyList();
                    logger.debug { "begin to refresh cache,now keyword ${keywords}" }

                    for (name in keywords) {

                        val cacheEntry = Cache.getCacheEntry(name)

                        logger.debug { "get ${name} , cacheEntry is ${cacheEntry}" }
                        //already expired
                        if (cacheEntry?.value != null) {
                            if (Instant.now().epochSecond - cacheEntry.timestamp > 0) {
                                logger.info { "${name} cache is expire, remove it" }
                                Cache.removeExpireCache(name)
                                continue
                            }
                        }

                        //get new message from api
                        val newResponse = searchHero(name)
                        val newResponseJson = JsonUtils.SearchHeroResponseToJson(newResponse)
                        val newResponseHash = EncryptionUtils.Md5(newResponseJson)

                        // no modify
                        if (cacheEntry?.hash == newResponseHash) {
                            logger.info { "no modify for ${name}" }
                            continue
                        }

                        logger.info { "${name} had been modified, previous hash: ${cacheEntry?.hash}, new hash: ${newResponseHash}" }
                        //cache have been modified
                        Cache.checkAndUpdate(name, newResponse)
                        //notify all
                        ChannelBasedFlowManager.notifyAll(name)
                    }
                } catch (e: Exception) {
                    logger.error { "Error while polling: ${e.message}" }
                }

                delay(AppConfig.getServerConfig().intervalMillis)
            }
        }


    }
}
