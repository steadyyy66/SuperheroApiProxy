package com.kody

import com.kody.config.AppConfig

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    //init config
    val config = AppConfig.getServerConfig()
    
    println("Server running on ${config.host}:${config.port}")
}