package com.github.petrvlcek.bisecur2mqtt

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.mock.declare

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class GroupCommandListenerTest : KoinTest {

    @MockK
    lateinit var gateway: BisecurGateway

    @MockK
    lateinit var mqttClient: MqttClient

    @SpyK
    var objectMapper = ObjectMapper()

    @BeforeEach
    fun beforeTest() {
        startKoin {
            modules(listOf(gatewayModule))
        }

        // override some components with mocks
        declare {
            single { gateway }
            single { objectMapper }
            single { mqttClient }
        }
    }

    @AfterEach
    fun afterTest() {
        stopKoin()
        clearMocks(gateway, objectMapper, mqttClient)
    }

    @Test
    fun handlSetAction() {
        every { gateway.setState(eq("somegroup")) } just Runs

        val messageListener = GroupCommandListener()
        val message = MqttMessage()

        runBlocking {
            messageListener.messageArrived("bisecur/group/somegroup/set", message)
        }

        coVerify {
            gateway.setState("somegroup")
        }
    }

    @Test
    fun handlGetAction() {
        every { gateway.getState(eq("somegroup")) } answers { State("somegroup", false, 0) }
        every { mqttClient.publish(eq("bisecur/group/somegroup/state"), any(), eq(0), eq(false)) } just Runs

        val messageListener = GroupCommandListener()
        val message = MqttMessage()

        runBlocking {
            messageListener.messageArrived("bisecur/group/somegroup/get", message)
        }

        val expectedPayload = objectMapper.writeValueAsBytes(State("somegroup", false, 0))
        coVerify {
            gateway.getState("somegroup")
            mqttClient.publish("bisecur/group/somegroup/state", expectedPayload, 0, false)
        }
    }

    @Test
    fun handleUnknnownAction() {
        every { gateway.setState(eq("somegroup")) } just Runs

        val messageListener = GroupCommandListener()
        val message = MqttMessage()

        runBlocking {
            messageListener.messageArrived("bisecur/group/somegroup/unknown", message)
        }

        coVerify {
            gateway wasNot Called
        }
    }
}