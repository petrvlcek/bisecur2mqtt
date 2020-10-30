package com.github.petrvlcek.bisecur2mqtt

import org.eclipse.paho.client.mqttv3.MqttClient
import org.koin.Logger.SLF4JLogger
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject

class App : KoinComponent {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            App().start()
        }
    }

    init {
        startKoin {
            SLF4JLogger()
            modules(listOf(envConfigModule, jsonModule))
            modules(listOf(gatewayModule))
            modules(listOf(mqttModule))
        }
    }

    private val mqttClient: MqttClient by inject()

    fun start() {
        mqttClient.subscribe("bisecur/group/#", GroupCommandListener())
    }
}