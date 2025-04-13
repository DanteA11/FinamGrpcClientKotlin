package ru.finam.client.defaultclient

import com.google.protobuf.Timestamp
import grpc.tradeapi.v1.GrpcSecurities
import proto.tradeapi.v1.*

/**
 * Интерфейс для взаимодействия с базовыми функциями GRPC Api Finam.
 */
interface ClientInterface {
    /**
     * Получение свечей.
     * Максимум 120 запросов в минуту.
     *
     * @param securityBoard Код площадки
     * @param securityCode Код инструмента
     * @param timeFrame Тайм-фрейм
     * @param interval Настройки интервала
     *
     * @return: Свечи
     */
    suspend fun getCandles(
        securityBoard: String,
        securityCode: String,
        timeFrame: ProtoCandles.DayCandleTimeFrame,
        interval: ProtoCandles.DayCandleInterval
    ): ProtoCandles.GetDayCandlesResult

    /**
     * Получение свечей.
     *
     * @param securityBoard Код площадки
     * @param securityCode Код инструмента
     * @param timeFrame Тайм-фрейм
     * @param interval Настройки интервала
     *
     * @return: Свечи
     */
    suspend fun getCandles(
        securityBoard: String,
        securityCode: String,
        timeFrame: ProtoCandles.IntradayCandleTimeFrame,
        interval: ProtoCandles.IntradayCandleInterval
    ): ProtoCandles.GetIntradayCandlesResult

    /**
     * Получение портфеля.
     * Максимум 100 запросов в минуту.
     *
     * @param clientId Торговый код клиента
     * @param includeCurrencies Запросить информацию по валютам портфеля
     * @param includeMoney Запросить информацию по денежным позициям портфеля
     * @param includePositions Запросить информацию по позициям портфеля
     * @param includeMaxBuySell Запросить информацию о максимальном
     * доступном объеме на покупку/продажу
     *
     * @return Модель портфеля
     */
    suspend fun getPortfolio(
        clientId: String,
        includeCurrencies: Boolean,
        includeMoney: Boolean,
        includePositions: Boolean,
        includeMaxBuySell: Boolean
    ): ProtoPortfolios.GetPortfolioResult

    /**
     * Получение списка инструментов.
     * Максимум 1 запрос в минуту.
     *
     * @param securityBoard Режим торгов. Можно передать пустую строку.
     * @param securityCode Тикер инструмента. Можно передать пустую строку.
     *
     * @return Модель инструментов
     */
    suspend fun getSecurities(
        securityBoard: String,
        securityCode: String
    ): GrpcSecurities.GetSecuritiesResult

    /**
     * Получение списка ордеров.
     * Максимум 100 запросов в минуту.
     *
     * @param clientId Торговый код клиента
     * @param includeActive Вернуть активные заявки
     * @param includeCanceled Вернуть отмененные заявки
     * @param includeMatched Вернуть исполненные заявки
     *
     * @return Модель ответа на запрос списка ордеров
     */
    suspend fun getOrders(
        clientId: String,
        includeActive: Boolean,
        includeCanceled: Boolean,
        includeMatched: Boolean
    ): ProtoOrders.GetOrdersResult

    /**
     * Получение списка ордеров.
     * Максимум 100 запросов в минуту.
     *
     * @param clientId Торговый код клиента
     *
     * @return Модель ответа на запрос списка ордеров
     */
    suspend fun getAllOrders(
        clientId: String
    ) = getOrders(clientId, includeActive = true, includeCanceled = true, includeMatched = true)

    /**
     * Получение списка активных ордеров.
     * Максимум 100 запросов в минуту.
     *
     * @param clientId Торговый код клиента
     *
     * @return Модель ответа на запрос списка ордеров
     */
    suspend fun getActiveOrders(clientId: String) = getOrders(
        clientId,
        includeActive = true,
        includeCanceled = false,
        includeMatched = false
    )

    /**
     * Получение списка стоп-ордеров.
     * Максимум 100 запросов в минуту.
     *
     * @param clientId Торговый код клиента
     * @param includeActive Вернуть активные стоп-заявки
     * @param includeCanceled Вернуть отмененные стоп-заявки
     * @param includeExecuted Вернуть исполненные стоп-заявки
     *
     * @return Модель ответа на запрос списка стоп-ордеров
     */
    suspend fun getStops(
        clientId: String,
        includeActive: Boolean,
        includeCanceled: Boolean,
        includeExecuted: Boolean,
    ): ProtoStops.GetStopsResult

    /**
     * Получение списка всех стоп-ордеров.
     * Максимум 100 запросов в минуту.
     *
     * @param clientId Торговый код клиента
     *
     * @return Модель ответа на запрос списка стоп-ордеров
     */
    suspend fun getAllStops(
        clientId: String
    ) = getStops(clientId, includeActive = true, includeCanceled = true, includeExecuted = true)

    /**
     * Получение списка активных стоп-ордеров.
     * Максимум 100 запросов в минуту.
     *
     * @param clientId Торговый код клиента
     *
     * @return Модель ответа на запрос списка стоп-ордеров
     */
    suspend fun getActiveStops(
        clientId: String
    ) = getStops(clientId, includeActive = true, includeCanceled = false, includeExecuted = false)

    /**
     * Создание нового ордера.
     *
     * @param clientId Торговый код клиента
     * @param securityBoard Режим торгов
     * @param securityCode Тикер инструмента
     * @param buySell Направление сделки
     * @param quantity Объем заявки в лотах
     * @param useCredit Использование кредита (недоступно для срочного рынка)
     * @param property Свойства исполнения частично исполненных заявок
     * @param price Цена заявки. Используйте null для выставления рыночной заявки
     * @param condition Типы условных ордеров
     * @param validBefore Условие по времени действия заявки
     *
     * @return Модель ответа на создание нового ордера
     */
    suspend fun createOrder(
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
    ): ProtoOrders.NewOrderResult

    /**
     * Создание нового ордера.
     *
     * @param clientId Торговый код клиента
     * @param securityBoard Режим торгов
     * @param securityCode Тикер инструмента
     * @param buySell Направление сделки
     * @param quantity Объем заявки в лотах
     * @param useCredit Использование кредита (недоступно для срочного рынка)
     * @param property Свойства исполнения частично исполненных заявок
     * @param price Цена заявки.
     *
     * @return Модель ответа на создание нового ордера
     */
    suspend fun createOrder(
        clientId: String,
        securityBoard: String,
        securityCode: String,
        buySell: ProtoCommon.BuySell,
        quantity: Int,
        useCredit: Boolean,
        property: ProtoOrders.OrderProperty,
        price: Double,
    ) = createOrder(
        clientId,
        securityBoard,
        securityCode,
        buySell,
        quantity,
        useCredit,
        property,
        price,
        null,
        null
    )

    /**
     * Создание нового рыночного ордера.
     *
     * @param clientId Торговый код клиента
     * @param securityBoard Режим торгов
     * @param securityCode Тикер инструмента
     * @param buySell Направление сделки
     * @param quantity Объем заявки в лотах
     * @param useCredit Использование кредита (недоступно для срочного рынка)
     * @param property Свойства исполнения частично исполненных заявок
     *
     * @return Модель ответа на создание нового ордера
     */
    suspend fun createOrder(
        clientId: String,
        securityBoard: String,
        securityCode: String,
        buySell: ProtoCommon.BuySell,
        quantity: Int,
        useCredit: Boolean,
        property: ProtoOrders.OrderProperty,
    ) = createOrder(
        clientId,
        securityBoard,
        securityCode,
        buySell,
        quantity,
        useCredit,
        property,
        null,
        null,
        null
    )


    suspend fun createOrder(
        clientId: String,
        securityBoard: String,
        securityCode: String,
        buySell: ProtoCommon.BuySell,
        quantity: Int,
        useCredit: Boolean,
        property: ProtoOrders.OrderProperty,
        condition: ProtoOrders.OrderCondition?,
    ) = createOrder(
        clientId,
        securityBoard,
        securityCode,
        buySell,
        quantity,
        useCredit,
        property,
        null,
        condition,
        null
    )

    /**
     * Создание нового стоп-ордера.
     *
     * @param clientId Торговый код клиента
     * @param securityBoard Режим торгов
     * @param securityCode Тикер инструмента
     * @param linkOrder Биржевой номер связанной (активной) заявки
     * @param buySell Направление сделки
     * @param stopLoss Cтоп лосс заявка
     * @param takeProfit Тейк профит заявка
     * @param expirationDate Дата экспирации заявки FORTS
     * @param validBefore Условие по времени действия заявки
     *
     * @return Модель ответа на создание нового стоп-ордера
     */
    suspend fun createStop(
        clientId: String,
        securityBoard: String,
        securityCode: String,
        linkOrder: Long,
        buySell: ProtoCommon.BuySell,
        stopLoss: ProtoStops.StopLoss?,
        takeProfit: ProtoStops.TakeProfit?,
        expirationDate: Timestamp?,
        validBefore: ProtoCommon.OrderValidBefore?
    ): ProtoStops.NewStopResult

    /**
     * Создание нового стоп-лосс ордера с параметрами по умолчанию.
     *
     * @param clientId Торговый код клиента
     * @param securityBoard Режим торгов
     * @param securityCode Тикер инструмента
     * @param linkOrder Биржевой номер связанной (активной) заявки
     * @param buySell Направление сделки
     * @param stopLoss Cтоп лосс заявка
     *
     * @return Модель ответа на создание нового стоп-ордера
     */
    suspend fun createStopLoss(
        clientId: String,
        securityBoard: String,
        securityCode: String,
        linkOrder: Long,
        buySell: ProtoCommon.BuySell,
        stopLoss: ProtoStops.StopLoss,
    ) = createStop(
        clientId,
        securityBoard,
        securityCode,
        linkOrder,
        buySell,
        stopLoss,
        null,
        null,
        null
    )
    /**
     * Создание нового тейк-профит ордера с параметрами по умолчанию.
     *
     * @param clientId Торговый код клиента
     * @param securityBoard Режим торгов
     * @param securityCode Тикер инструмента
     * @param linkOrder Биржевой номер связанной (активной) заявки
     * @param buySell Направление сделки
     * @param takeProfit Тейк профит заявка
     *
     * @return Модель ответа на создание нового стоп-ордера
     */
    suspend fun createTakeProfit(
        clientId: String,
        securityBoard: String,
        securityCode: String,
        linkOrder: Long,
        buySell: ProtoCommon.BuySell,
        takeProfit: ProtoStops.TakeProfit,
    ) = createStop(
        clientId,
        securityBoard,
        securityCode,
        linkOrder,
        buySell,
        null,
        takeProfit,
        null,
        null
    )

    /**
     * Отмена ордера.
     *
     * Важно: если к лимитной заявке была привязана стоп-заявка,
     * то стоп-заявка не будет отменена, пока есть еще
     * лимитные заявки по инструменту.
     *
     * @param clientId Торговый код клиента
     * @param transactionId Идентификатор отменяемой заявки
     *
     * @return Модель ответа на отмену ордера
     */
    suspend fun cancelOrder(
        clientId: String,
        transactionId: Int
    ): ProtoOrders.CancelOrderResult

    /**
     * Отмена стоп-ордера.
     *
     * @param clientId Торговый код клиента
     * @param stopId Идентификатор отменяемой стоп-заявки
     *
     * @return Модель ответа на отмену стоп-ордера
     */
    suspend fun cancelStops(
        clientId: String,
        stopId: Int
    ): ProtoStops.CancelStopResult

    /**
     * Действия перед остановкой работы
     */
    fun stop()
}