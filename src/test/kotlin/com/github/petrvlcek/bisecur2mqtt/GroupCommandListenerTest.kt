package com.github.petrvlcek.bisecur2mqtt

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
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

    @BeforeEach
    fun beforeTest() {
        startKoin {
            modules(listOf(gatewayModule))
        }

        // override some components with mocks
        declare {
            single { gateway }
        }
    }

    @AfterEach
    fun afterTest() {
        stopKoin()
        clearMocks(gateway)
    }

    @Test
    fun handlSetAction() {
        every { gateway.setState(eq("somegroup")) } just Runs

        val messageListener = GroupCommandListener()
        val message = MqttMessage()

        messageListener.messageArrived("bisecur/group/somegroup/set", message)

        verify {
            gateway.setState("somegroup")
        }
    }

    @Test
    fun handleUnknnownAction() {
        every { gateway.setState(eq("somegroup")) } just Runs

        val messageListener = GroupCommandListener()
        val message = MqttMessage()

        messageListener.messageArrived("bisecur/group/somegroup/unknown", message)

        verify {
            gateway wasNot Called
        }
    }
}