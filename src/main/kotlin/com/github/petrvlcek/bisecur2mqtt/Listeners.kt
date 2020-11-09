package com.github.petrvlcek.bisecur2mqtt

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.koin.core.inject

class GroupCommandListener : MessageListener() {
    private val logger = KotlinLogging.logger {}
    private val gateway: BisecurGateway by inject()
    private val objectMapper: ObjectMapper by inject()

    private val actionPattern = """\/{0,1}bisecur\/group\/([\w\s]+)\/([\w]+)""".toRegex()

    override fun handleMessage(topic: String?, payload: ByteArray?) {
        if (actionPattern.matches("${topic}")) {
            val matchResult = actionPattern.find("${topic}")
            val (groupName, action) = matchResult!!.destructured
            logger.info { "received action command, group name: ${groupName}, action: ${action}" }
            when (action) {
                "set" -> handleSet(groupName)
                "get" -> handleGet(groupName)
                else -> {
                    logger.warn { "Action ${action} not recognized." }
                }
            }
        }
    }

    private fun handleSet(groupName: String) {
        gateway.setState(groupName)
    }

    private fun handleGet(groupName: String) {
        val state = gateway.getState(groupName)
        publish("bisecur/group/${groupName}/state", objectMapper.writeValueAsBytes(state))
    }

}