package com.github.petrvlcek.bisecur2mqtt

import mu.KotlinLogging
import org.koin.dsl.module

val envConfigModule = module {
    single<Config> { EnvConfig() }
}

interface Config {
    val gatewayId : String
    val gatewayAddress : String
    val gatewaySenderId : String
    val gatewayUsername : String
    val gatewayPassword : String

    val mqttServerUri : String
    val mqttClientId : String
    val mqttUsername : String
    val mqttPassword : String
}

class EnvConfig : Config {
    override val gatewayId by lazy { getRequiredEnv("GATEWAY_ID") }
    override val gatewayAddress by lazy { getRequiredEnv("GATEWAY_ADDRESS") }
    override val gatewaySenderId by lazy { getRequiredEnv("GATEWAY_SENDER_ID") }
    override val gatewayUsername by lazy { getRequiredEnv("GATEWAY_USERNAME") }
    override val gatewayPassword by lazy { getRequiredEnv("GATEWAY_PASSWORD") }

    override val mqttServerUri by lazy { getRequiredEnv("MQTT_SERVER_URI") }
    override val mqttClientId by lazy { getRequiredEnv("MQTT_CLIENT_ID") }
    override val mqttUsername by lazy { getRequiredEnv("MQTT_PASSWORD") }
    override val mqttPassword by lazy { getRequiredEnv("MQTT_PASSWORD") }

    private val logger = KotlinLogging.logger {}

    private fun getRequiredEnv(name: String): String {
        logger.debug { "Retrieving environment variable: $name" }
        return System.getenv(name) ?: run {
            logger.error { "Missing env var '$name'!" }
            throw IllegalStateException("Missing env var '$name'!")
        }
    }
}