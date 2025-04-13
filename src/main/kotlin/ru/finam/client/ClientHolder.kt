package ru.finam.client

import ru.finam.client.defaultclient.FinamGrpcDefaultClient
import ru.finam.client.defaultclient.ClientInterface
import ru.finam.client.subscribe.FinamGrpcSubscribeClient
import ru.finam.client.subscribe.SubscribeClientInterface

object ClientHolder {
    private const val URL = "trade-api.finam.ru"

    fun getDefaultClient(
        token: String, url: String = URL
    ): ClientInterface {
        return FinamGrpcDefaultClient(token, url)
    }

    fun getSubscribeClient(
        token: String,
        url: String = URL,
        keepAliveRequestId: String = "keepAliveRequest"
    ): SubscribeClientInterface {
        return FinamGrpcSubscribeClient(token, url, keepAliveRequestId)
    }
}