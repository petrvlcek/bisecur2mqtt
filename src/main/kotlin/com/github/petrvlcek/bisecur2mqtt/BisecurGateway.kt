package com.github.petrvlcek.bisecur2mqtt

import mu.KotlinLogging
import org.bisdk.sdk.ClientAPI
import org.bisdk.sdk.GatewayConnection
import org.bisdk.sdk.Group
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

data class State(
    val groupName: String,
    val isOpen: Boolean,
    val stateInPercent: Int,
    val isError: Boolean = false,
    val autoClose: Boolean = false
)

class BisecurGateway : KoinComponent {
    private val config: Config by inject()
    private val logger = KotlinLogging.logger {}
    lateinit private var clientAPI: ClientAPI
    lateinit private var userGroups: List<Group>

    fun connect() {
        //        val discovery = Discovery()
        //        val future = discovery.startServer()
        //        discovery.sendDiscoveryRequest()
        //        val discoveryData = future.join()
        //        val client = GatewayConnection(discoveryData.sourceAddress, "000000000000", discoveryData.getGatewayId())

        val client =
            GatewayConnection(InetAddress.getByName(config.gatewayAddress), config.gatewaySenderId, config.gatewayId)
        clientAPI = ClientAPI(client)
        logger.info("BiSecur Gateway, action=gateway_ping, name=${clientAPI.getName()}, ping=${clientAPI.ping()}")
        clientAPI.login(config.gatewayUsername, config.gatewayPassword)
        logger.debug("Getting groups for current user, action=get_user_groups, username=${config.gatewayUsername}")
        userGroups = clientAPI.getGroupsForUser()
            .let {
                logger.info { "Groups for current user: action=get_user_groups, username=${config.gatewayUsername}, groups=${it}" }
                it
            }
    }

    fun setState(groupName: String) {
        userGroups.find { group -> group.name.equals(groupName, true) }
            ?.let {
                clientAPI.setState(it.ports[0])
            }
    }

    fun getState(groupName: String): State? {
        return userGroups.find { group -> group.name.equals(groupName, true) }
            ?.let {
                val transition = clientAPI.getTransition(it.ports[0])
                State(
                    groupName,
                    transition.hcp.positionOpen,
                    transition.stateInPercent,
                    transition.error,
                    transition.autoClose
                )
            }
    }
}