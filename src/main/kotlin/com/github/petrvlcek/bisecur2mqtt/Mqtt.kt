package com.github.petrvlcek.bisecur2mqtt

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.dsl.module

val mqttModule = module {
    single {
        val config: Config = get()

        val mqttClient = MqttClient(config.mqttServerUri, config.mqttClientId, MemoryPersistence())
        val options = MqttConnectOptions()
        options.keepAliveInterval = 180
        options.isCleanSession = true
        options.userName = config.mqttUsername
        options.password = config.mqttPassword.toCharArray()
        mqttClient.connect(options)
        mqttClient
    }
}

abstract class MessageListener : KoinComponent, IMqttMessageListener {
    private val logger = KotlinLogging.logger {}
    private val mqttClient: MqttClient by inject()

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        logger.info { "got message, topic: ${topic}, messageId: ${message?.id}, message: ${message}" }

        GlobalScope.launch {
            try {
                handleMessage(topic, message?.payload)
            } catch (e: Exception) {
                logger.error("got error when handling message, topic: ${topic}, messageId: ${message?.id}", e)
            }
        }
    }

    abstract fun handleMessage(topic: String?, message: ByteArray?)

    fun publish(topic: String, payload: ByteArray) {
        mqttClient.publish(topic, payload, 0, false)
    }
}
