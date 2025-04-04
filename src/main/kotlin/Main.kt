package com.kody

import com.kody.cache.Cache
import com.kody.config.AppConfig
import com.kody.service.SuperHeroService
import io.grpc.ServerBuilder

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    //init config
    AppConfig.init()
    val config = AppConfig.getServerConfig()


    // 创建组件
    val service = SuperHeroService()

    // 启动 gRPC 服务器
    val server = ServerBuilder
        .forPort(config.port)
        .addService(service)
        .build()

    server.start()
    println("Server started on port ${config.port}")
    server.awaitTermination()
    println("Server running on ${config.host}:${config.port}")

}