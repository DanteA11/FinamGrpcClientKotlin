package ru.finam.subscribe

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import ru.finam.ClientHolder

abstract class AbstractTest {

    companion object {
        @JvmStatic
        private val token = System.getenv("FINAM_USER_TOKEN")!!
        @JvmStatic
        protected val clientId = System.getenv("FINAM_CLIENT_ID")!!

        @JvmStatic
        lateinit var client: SubscribeClientInterface

        @JvmStatic
        @BeforeAll
        fun start() {
            client = ClientHolder.getSubscribeClient(token)
        }

        @JvmStatic
        @AfterAll
        fun stop() {
            client.stop()
        }
    }
}