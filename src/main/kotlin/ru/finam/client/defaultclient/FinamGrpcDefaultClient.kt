package ru.finam.client.defaultclient

import com.google.protobuf.DoubleValue
import com.google.protobuf.StringValue
import com.google.protobuf.Timestamp
import grpc.tradeapi.v1.CandlesGrpcKt.CandlesCoroutineStub
import grpc.tradeapi.v1.GrpcSecurities
import grpc.tradeapi.v1.OrdersGrpcKt.OrdersCoroutineStub
import grpc.tradeapi.v1.PortfoliosGrpcKt.PortfoliosCoroutineStub
import grpc.tradeapi.v1.SecuritiesGrpcKt.SecuritiesCoroutineStub
import grpc.tradeapi.v1.StopsGrpcKt.StopsCoroutineStub
import io.github.oshai.kotlinlogging.KotlinLogging
import proto.tradeapi.v1.*


open class FinamGrpcDefaultClient(
    token: String,
    url: String = "trade-api.finam.ru"
) : AbstractFinamGrpcClient(token, url),
    ClientInterface {
    override val logger = KotlinLogging.logger {}

    private val candles = CandlesCoroutineStub(channel)
    private val orders = OrdersCoroutineStub(channel)
    private val stops = StopsCoroutineStub(channel)
    private val portfolio = PortfoliosCoroutineStub(channel)
    private val securities = SecuritiesCoroutineStub(channel)

    override suspend fun getCandles(
        securityBoard: String,
        securityCode: String,
        timeFrame: ProtoCandles.DayCandleTimeFrame,
        interval: ProtoCandles.DayCandleInterval
    ): ProtoCandles.GetDayCandlesResult {
        logger.info { "Запрос получения дневных свечей: securityBoard=$securityBoard, securityCode=$securityCode, timeFrame=$timeFrame, interval=$interval" }
        val modelBuilder = ProtoCandles.GetDayCandlesRequest.newBuilder()
        modelBuilder
            .setSecurityBoard(securityBoard)
            .setSecurityCode(securityCode)
            .setTimeFrame(timeFrame)
            .setInterval(interval)
        val result = executeRequest(modelBuilder.build(), candles::getDayCandles)
        logger.debug { "Получены свечи: $result" }
        return result
    }

    override suspend fun getCandles(
        securityBoard: String,
        securityCode: String,
        timeFrame: ProtoCandles.IntradayCandleTimeFrame,
        interval: ProtoCandles.IntradayCandleInterval
    ): ProtoCandles.GetIntradayCandlesResult {
        logger.info { "Запрос получения внутридневных свечей: securityBoard=$securityBoard, securityCode=$securityCode, timeFrame=$timeFrame, interval=$interval" }
        val modelBuilder = ProtoCandles.GetIntradayCandlesRequest.newBuilder()
        modelBuilder
            .setSecurityBoard(securityBoard)
            .setSecurityCode(securityCode)
            .setTimeFrame(timeFrame)
            .setInterval(interval)
        val result = executeRequest(modelBuilder.build(), candles::getIntradayCandles)
        logger.debug { "Получены свечи: $result" }
        return result
    }

    override suspend fun getPortfolio(
        clientId: String,
        includeCurrencies: Boolean,
        includeMoney: Boolean,
        includePositions: Boolean,
        includeMaxBuySell: Boolean
    ): ProtoPortfolios.GetPortfolioResult {
        logger.info { "Запрос информации о портфеле: clientId=$clientId, includeCurrencies=$includeCurrencies, includeMoney=$includeMoney, includePositions=$includePositions, includeMaxBuySell=$includeMaxBuySell." }
        val modelBuilder = ProtoPortfolios.GetPortfolioRequest.newBuilder()
        modelBuilder.setClientId(clientId)
        modelBuilder.contentBuilder
            .setIncludeCurrencies(includeCurrencies)
            .setIncludeMoney(includeMoney)
            .setIncludePositions(includePositions)
            .setIncludeMaxBuySell(includeMaxBuySell)
        val result = executeRequest(modelBuilder.build(), portfolio::getPortfolio)
        logger.debug { "Получена информация о портфеле: $result" }
        return result
    }

    override suspend fun getSecurities(
        securityBoard: String,
        securityCode: String
    ): GrpcSecurities.GetSecuritiesResult {
        logger.info { "Запрос информации об инструменте: securityBoard=$securityBoard, securityCode=$securityCode" }
        val modelBuilder = GrpcSecurities.GetSecuritiesRequest.newBuilder()
        modelBuilder.setBoard(StringValue.of(securityBoard))
            .setSeccode(StringValue.of(securityCode))
        val result = executeRequest(modelBuilder.build(), securities::getSecurities)
        logger.debug { "Получена информация об инструменте: $result" }
        return result
    }

    override suspend fun getOrders(
        clientId: String,
        includeActive: Boolean,
        includeCanceled: Boolean,
        includeMatched: Boolean
    ): ProtoOrders.GetOrdersResult {
        logger.info { "Запрос на получение ордеров: clientId=$clientId, includeActive=$includeActive, includeCanceled=$includeCanceled, includeMatched=$includeMatched" }
        val modelBuilder = ProtoOrders.GetOrdersRequest.newBuilder()
        modelBuilder.setClientId(clientId)
            .setIncludeActive(includeActive)
            .setIncludeCanceled(includeCanceled)
            .setIncludeMatched(includeMatched)
        val result = executeRequest(modelBuilder.build(), orders::getOrders)
        logger.debug { "Получены ордера: $result" }
        return result
    }

    override suspend fun getStops(
        clientId: String,
        includeActive: Boolean,
        includeCanceled: Boolean,
        includeExecuted: Boolean,
    ): ProtoStops.GetStopsResult {
        logger.info { "Запрос на получение стоп-ордеров: clientId=$clientId, includeActive=$includeActive, includeCanceled=$includeCanceled, includeExecuted=$includeExecuted" }
        val modelBuilder = ProtoStops.GetStopsRequest.newBuilder()
        modelBuilder.setClientId(clientId)
            .setIncludeActive(includeActive)
            .setIncludeCanceled(includeCanceled)
            .setIncludeExecuted(includeExecuted)
        val result = executeRequest(modelBuilder.build(), stops::getStops)
        logger.debug { "Получены стоп-ордера: $result" }
        return result
    }

    override suspend fun createOrder(
        clientId: String,
        securityBoard: String,
        securityCode: String,
        buySell: ProtoCommon.BuySell,
        quantity: Int,
        useCredit: Boolean,
        property: ProtoOrders.OrderProperty,
        price: Double?,
        condition: ProtoOrders.OrderCondition?,
        validBefore: ProtoCommon.OrderValidBefore?
    ): ProtoOrders.NewOrderResult {
        logger.info { "Запрос на создание нового ордера: clientId=$clientId, securityBoard=$securityBoard, securityCode=$securityCode, buySell=$buySell, quantity=$quantity, useCredit=$useCredit, property=$property, price=$price, condition=$condition, validBefore=$validBefore" }
        val modelBuilder = ProtoOrders.NewOrderRequest.newBuilder()
        modelBuilder.setClientId(clientId)
            .setSecurityBoard(securityBoard)
            .setSecurityCode(securityCode)
            .setBuySell(buySell)
            .setQuantity(quantity)
            .setUseCredit(useCredit)
            .setProperty(property)
        if (price != null)
            modelBuilder.setPrice(DoubleValue.of(price))
        if (condition != null)
            modelBuilder.setCondition(condition)
        if (validBefore != null)
            modelBuilder.setValidBefore(validBefore)
        val result = executeRequest(modelBuilder.build(), orders::newOrder)
        logger.debug { "Получен ответ на создание нового ордера: $result" }
        return result
    }

    override suspend fun createStop(
        clientId: String,
        securityBoard: String,
        securityCode: String,
        linkOrder: Long,
        buySell: ProtoCommon.BuySell,
        stopLoss: ProtoStops.StopLoss?,
        takeProfit: ProtoStops.TakeProfit?,
        expirationDate: Timestamp?,
        validBefore: ProtoCommon.OrderValidBefore?
    ): ProtoStops.NewStopResult {
        logger.info { "Запрос на создание нового стоп-ордера: clientId=$clientId, securityBoard=$securityBoard, securityCode=$securityCode, buySell=$buySell, stopLoss=$stopLoss, takeProfit=$takeProfit, expirationDate=$expirationDate, validBefore=$validBefore" }
        val modelBuilder = ProtoStops.NewStopRequest.newBuilder()
        modelBuilder.setClientId(clientId)
            .setSecurityBoard(securityBoard)
            .setSecurityCode(securityCode)
            .setLinkOrder(linkOrder)
            .setBuySell(buySell)
        if (stopLoss != null)
            modelBuilder.setStopLoss(stopLoss)
        if (takeProfit != null)
            modelBuilder.setTakeProfit(takeProfit)
        if (expirationDate != null)
            modelBuilder.setExpirationDate(expirationDate)
        if (validBefore != null)
            modelBuilder.setValidBefore(validBefore)
        val result = executeRequest(modelBuilder.build(), stops::newStop)
        logger.debug { "Получен ответ на создание нового стоп-ордера: $result" }
        return result
    }

    override suspend fun cancelOrder(
        clientId: String,
        transactionId: Int
    ): ProtoOrders.CancelOrderResult {
        logger.info { "Запрос на отмену ордера: clientId=$clientId, transactionId=$transactionId" }
        val modelBuilder = ProtoOrders.CancelOrderRequest.newBuilder()
        modelBuilder.setClientId(clientId)
            .setTransactionId(transactionId)
        val result = executeRequest(modelBuilder.build(), orders::cancelOrder)
        logger.debug { "Получен ответ на отмену ордера: $result" }
        return result
    }

    override suspend fun cancelStops(
        clientId: String,
        stopId: Int
    ): ProtoStops.CancelStopResult {
        logger.info { "Запрос на отмену стоп-ордера: clientId$clientId, stopId=$stopId" }
        val modelBuilder = ProtoStops.CancelStopRequest.newBuilder()
        modelBuilder.setClientId(clientId)
            .setStopId(stopId)
        val result = executeRequest(modelBuilder.build(), stops::cancelStop)
        logger.debug { "Получен ответ на отмену стоп-ордера: $result" }
        return result
    }
}