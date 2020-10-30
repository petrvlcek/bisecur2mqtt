package com.github.petrvlcek.bisecur2mqtt

import mu.KotlinLogging
import org.bisdk.sdk.ClientAPI
import org.bisdk.sdk.GatewayConnection
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.dsl.module
import java.net.InetAddress

val gatewayModule = module {
    single {
        val gateway = BisecurGateway()
        gateway.connect()
        gateway
    }
}

class BisecurGateway : KoinComponent {
    private val config: Config by inject()
    private val logger = KotlinLogging.logger {}
    lateinit private var clientAPI: ClientAPI

    fun connect() {
        //        val discovery = Discovery()
        //        val future = discovery.startServer()
        //        discovery.sendDiscoveryRequest()
        //        val discoveryData = future.join()
        //        val client = GatewayConnection(discoveryData.sourceAddress, "000000000000", discoveryData.getGatewayId())

        val client =
            GatewayConnection(InetAddress.getByName(config.gatewayAddress), config.gatewaySenderId, config.gatewayId)
        clientAPI = ClientAPI(client)
        logger.info("BiSecur Gateway, name=${clientAPI.getName()}, ping=${clientAPI.ping()}")
        clientAPI.login(config.gatewayUsername, config.gatewayPassword)
    }

    fun setState(groupName: String) {
        clientAPI.getGroupsForUser()
            .let {
                logger.debug { "Groups: ${it}" }
                it
            }
            .find { group -> group.name.equals(groupName, true) }
            ?.let {
                clientAPI.setState(it.ports[0])
            }

    }

    fun getTransition(groupName: String) {
        clientAPI.getGroupsForUser()
            .let {
                logger.debug { "Groups: ${it}" }
                it
            }
            .find { group -> group.name.equals(groupName, true) }
            ?.let {
                clientAPI.getTransition(it.ports[0])
            }
    }
}