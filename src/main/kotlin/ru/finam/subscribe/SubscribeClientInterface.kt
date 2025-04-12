package ru.finam.subscribe

import proto.tradeapi.v1.ProtoCommon.ResponseEvent
import proto.tradeapi.v1.ProtoEvents
import ru.finam.defaultclient.ClientInterface

/**
 * Интерфейс для взаимодействия с подписками Finam GRPC Api.
 */
interface SubscribeClientInterface : ClientInterface {
    var onOrder: suspend (ProtoEvents.OrderEvent) -> Unit
    var onTrade: suspend (ProtoEvents.TradeEvent) -> Unit
    var onOrderBook: suspend (ProtoEvents.OrderBookEvent) -> Unit
    var onPortfolio: suspend (ProtoEvents.PortfolioEvent) -> Unit
    var onResponse: suspend (ResponseEvent) -> Unit

    /**
     * Подписка на заявки и сделки
     *
     * @param requestId ID запроса
     * @param clientIds ID счетов
     * @param includeTrades Включить информацию о сделках
     * @param includeOrders Включить информацию о заявках
     */
    suspend fun subscribeOrdersTrades(
        requestId: String,
        clientIds: Iterable<String>,
        includeTrades: Boolean,
        includeOrders: Boolean
    )

    /**
     * Подписка на заявки и сделки
     *
     * @param requestId ID запроса
     * @param clientIds ID счетов
     */
    suspend fun subscribeOrdersTradesAll(
        requestId: String,
        clientIds: Iterable<String>
    ) {
        return subscribeOrdersTrades(
            requestId, clientIds,
            includeTrades = true,
            includeOrders = true
        )
    }

    /**
     * Отмена подписки на ордера и сделки
     *
     * @param requestId ID запроса
     */
    suspend fun unsubscribeOrdersTrades(requestId: String)

    /**
     * Подписка на стакан
     *
     * @param requestId ID запроса
     * @param securityBoard Код площадки
     * @param securityCode Код инструмента
     */
    suspend fun subscribeOrderBook(
        requestId: String,
        securityBoard: String,
        securityCode: String
    )

    /**
     * Отмена подписки на стакан
     *
     * @param requestId ID запроса
     * @param securityBoard Код площадки
     * @param securityCode Код инструмента
     */
    suspend fun unsubscribeOrderBook(
        requestId: String,
        securityBoard: String,
        securityCode: String
    )
}