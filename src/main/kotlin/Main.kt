package com.kody

import com.kody.config.AppConfig
import com.kody.config.ServerConfig
import com.kody.service.SuperHeroService
import io.grpc.ServerBuilder
import io.prometheus.client.exporter.HTTPServer
import io.prometheus.client.hotspot.DefaultExports

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    //init config
    val config = initConfig()
    //init metrics
    startMetricsServer(config.prometheusPort)
    //init grpc
    val service = SuperHeroService()
    val server = ServerBuilder
        .forPort(config.grpcPort)
        .addService(service)
        .build()

    server.start()
    println("Server started on port ${config.grpcPort}")
    server.awaitTermination()

}

fun initConfig(): ServerConfig {
    AppConfig.init()
    val config = AppConfig.getServerConfig()
    return config
}
fun startMetricsServer(port: Int) {

//    DefaultExports.initialize()
    HTTPServer(port)
}