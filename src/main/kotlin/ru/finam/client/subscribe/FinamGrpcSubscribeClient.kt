package ru.finam.client.subscribe

import com.google.protobuf.GeneratedMessageV3
import grpc.tradeapi.v1.EventsGrpcKt.EventsCoroutineStub
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.consumeAsFlow
import proto.tradeapi.v1.ProtoCommon.ResponseEvent
import proto.tradeapi.v1.ProtoEvents
import ru.finam.client.defaultclient.FinamGrpcDefaultClient
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel as Ch

class FinamGrpcSubscribeClient(
    token: String,
    url: String = "trade-api.finam.ru",
    private val keepAliveRequestId: String = "keepAliveRequest",
) : FinamGrpcDefaultClient(token, url), SubscribeClientInterface {

    override val logger = KotlinLogging.logger {}

    private val events = EventsCoroutineStub(channel)
    private val outgoingChannel = Ch<ProtoEvents.SubscriptionRequest>(Ch.UNLIMITED)
    private val forComparison = listOf(
        ProtoEvents.OrderEvent.newBuilder().build(),
        ProtoEvents.TradeEvent.newBuilder().build(),
        ProtoEvents.OrderBookEvent.newBuilder().build(),
        ProtoEvents.PortfolioEvent.newBuilder().build(),
        ResponseEvent.newBuilder().build()
    )
    private val mainScope = CoroutineScope(Dispatchers.Default)

    override var onOrder: suspend (ProtoEvents.OrderEvent) -> Unit = this::defaultCallback
    override var onTrade: suspend (ProtoEvents.TradeEvent) -> Unit = this::defaultCallback
    override var onOrderBook: suspend (ProtoEvents.OrderBookEvent) -> Unit = this::defaultCallback
    override var onPortfolio: suspend (ProtoEvents.PortfolioEvent) -> Unit = this::defaultCallback
    override var onResponse: suspend (ResponseEvent) -> Unit = this::defaultCallback

    init {
        logger.info { "Запуск обработки подписок" }
        mainScope.launch { subscriptionHandler() }
        logger.info { "Запуск поддержания активности" }
        mainScope.launch { keepAliveHandler() }
    }

    override fun stop() {
        mainScope.cancel()
        logger.info { "Поток обработки подписок остановлен" }
        super.stop()
    }

    suspend fun <T : GeneratedMessageV3> defaultCallback(event: T) {}

    /**
     * Обработка поступающего события.
     * Вызывает необходимый Callback при поступлении события.
     *
     * @param event Входящее событие.
     */
    private suspend fun onEvent(event: ProtoEvents.Event) {
        logger.debug { "Пришло событие: $event" }
        if (!event.order.equals(forComparison[0]))
            onOrder(event.order)
        else if (!event.trade.equals(forComparison[1]))
            onTrade(event.trade)
        else if (!event.orderBook.equals(forComparison[2]))
            onOrderBook(event.orderBook)
        else if (!event.portfolio.equals(forComparison[3]))
            onPortfolio(event.portfolio)
        else if (!event.response.equals(forComparison[4]))
            onResponse(event.response)
    }

    /**
     * Функция для обработки подписок
     */
    private suspend fun subscriptionHandler() {
        events.getEvents(outgoingChannel.consumeAsFlow(), metadata)
            .collect { message ->
                mainScope.launch { onEvent(message) }
            }
    }

    /**
     * Функция для отправки запроса на поддержание активности
     */
    private suspend fun keepAliveHandler() {
        val request = ProtoEvents.SubscriptionRequest.newBuilder()
        request.setKeepAliveRequest(
            ProtoEvents.KeepAliveRequest.newBuilder()
                .setRequestId(keepAliveRequestId)
        )
        while (true) {
            delay(120.seconds)
            outgoingChannel.send(request.build())
            logger.debug { "Отправлен запрос на поддержание активности" }
        }
    }

    override suspend fun subscribeOrdersTrades(
        requestId: String,
        clientIds: Iterable<String>,
        includeTrades: Boolean,
        includeOrders: Boolean
    ) {
        logger.info { "Подписка на заявки и сделки: requestId=$requestId, clientIds=$clientIds, includeTrades=$includeTrades, includeOrders=$includeOrders" }
        val request = ProtoEvents.SubscriptionRequest.newBuilder()
        val modelBuilder = ProtoEvents.OrderTradeSubscribeRequest.newBuilder()
        modelBuilder.setRequestId(requestId)
            .setIncludeTrades(includeTrades)
            .setIncludeOrders(includeOrders)
            .addAllClientIds(clientIds)
        request.setOrderTradeSubscribeRequest(modelBuilder)
        outgoingChannel.send(request.build())
    }

    override suspend fun unsubscribeOrdersTrades(requestId: String) {
        logger.info { "Отмена подписки на заявки и сделки: requestId=$requestId" }
        val request = ProtoEvents.SubscriptionRequest.newBuilder()
        val modelBuilder = ProtoEvents.OrderTradeUnsubscribeRequest.newBuilder()
        modelBuilder.setRequestId(requestId)
        request.setOrderTradeUnsubscribeRequest(modelBuilder)
        outgoingChannel.send(request.build())
    }

    override suspend fun subscribeOrderBook(
        requestId: String,
        securityBoard: String,
        securityCode: String
    ) {
        logger.info { "Подписка на стакан: requestId=$requestId, securityBoard=$securityBoard, securityCode=$securityCode" }
        val request = ProtoEvents.SubscriptionRequest.newBuilder()
        val modelBuilder = ProtoEvents.OrderBookSubscribeRequest.newBuilder()
        modelBuilder.setRequestId(requestId)
            .setSecurityBoard(securityBoard)
            .setSecurityCode(securityCode)
        request.setOrderBookSubscribeRequest(modelBuilder)
        outgoingChannel.send(request.build())
    }

    override suspend fun unsubscribeOrderBook(
        requestId: String,
        securityBoard: String,
        securityCode: String
    ) {
        logger.info { "Отмена подписки на стакан: requestId=$requestId, securityBoard=$securityBoard, securityCode=$securityCode" }
        val request = ProtoEvents.SubscriptionRequest.newBuilder()
        val modelBuilder = ProtoEvents.OrderBookUnsubscribeRequest.newBuilder()
        modelBuilder.setRequestId(requestId)
            .setSecurityBoard(securityBoard)
            .setSecurityCode(securityCode)
        request.setOrderBookUnsubscribeRequest(modelBuilder)
        outgoingChannel.send(request.build())
    }
}

