package ru.finam

import ru.finam.defaultclient.FinamGrpcDefaultClient
import ru.finam.defaultclient.ClientInterface
import ru.finam.subscribe.FinamGrpcSubscribeClient
import ru.finam.subscribe.SubscribeClientInterface

object ClientHolder {
    fun getDefaultClient(
        token: String, url: String = "trade-api.finam.ru"
    ): ClientInterface {
        return FinamGrpcDefaultClient(token, url)
    }

    fun getSubscribeClient(
        token: String,
        url: String = "trade-api.finam.ru",
        keepAliveRequestId: String = "keepAliveRequest"
    ): SubscribeClientInterface {
        return FinamGrpcSubscribeClient(token, url, keepAliveRequestId)
    }
}