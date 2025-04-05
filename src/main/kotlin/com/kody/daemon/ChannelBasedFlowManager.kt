package com.kody.daemon

import com.kody.grpc.SubscribeResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

object ChannelBasedFlowManager {
    private val subscribers = mutableListOf<SendChannel<SubscribeResponse>>()

    @Synchronized
    fun registerNewSubscriber(): Flow<SubscribeResponse> {
        val channel = Channel<SubscribeResponse>(capacity = Channel.UNLIMITED)
        subscribers.add(channel)


        return callbackFlow {
            val job = launch {
                for (msg in channel) {
                    send(msg)
                }
            }

            // 当客户端取消订阅
            awaitClose {
                channel.close()
                removeSubscriber(channel)
                job.cancel()
            }
        }
    }

    @Synchronized
    private fun removeSubscriber(channel: SendChannel<SubscribeResponse>) {
        subscribers.remove(channel)
    }

    @Synchronized
    fun notifyAll(keyword: String) {
        val response = SubscribeResponse.newBuilder()
            .setKeyword(keyword)
            .build()

        val toRemove = mutableListOf<SendChannel<SubscribeResponse>>()

        for (subscriber in subscribers) {
            try {
                subscriber.trySend(response)
            } catch (e: Exception) {
                toRemove.add(subscriber)
            }
        }

        // 清理失效连接
        for (subscriber in toRemove) {
            removeSubscriber(subscriber)
        }
    }
}
