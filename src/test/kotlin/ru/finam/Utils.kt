package ru.finam

import com.google.protobuf.Timestamp
import kotlinx.coroutines.runBlocking
import proto.tradeapi.v1.ProtoCandles
import proto.tradeapi.v1.ProtoCommon
import proto.tradeapi.v1.ProtoOrders
import proto.tradeapi.v1.ProtoStops
import ru.finam.defaultclient.ClientInterface
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.pow

object Utils {
    private const val PRICE_MIN_MULTIPLIER = 0.97
    private const val PRICE_MAX_MULTIPLIER = 1.03

    fun createMarketOrder(
        client: ClientInterface,
        clientId: String,
        securityBoard: String,
        securityCode: String,
        buySell: ProtoCommon.BuySell
    ) = runBlocking {
        val property = ProtoOrders.OrderProperty.ORDER_PROPERTY_PUT_IN_QUEUE
        val order = client.createOrder(
            clientId,
            securityBoard,
            securityCode,
            buySell,
            1,
            true,
            property
        )
        return@runBlocking order
    }

    fun createConditionalOrder(
        client: ClientInterface,
        clientId: String,
        securityBoard: String,
        securityCode: String,
        buySell: ProtoCommon.BuySell
    ) = runBlocking {
        val condition = ProtoOrders.OrderCondition.newBuilder()
        val property = ProtoOrders.OrderProperty.ORDER_PROPERTY_PUT_IN_QUEUE
        val priceScale = getLastClosePriceScale(client, securityBoard, securityCode)
        var price = priceScale[0] as Double
        val scale = priceScale[1] as Int
        if (buySell == ProtoCommon.BuySell.BUY_SELL_BUY) {
            price *= PRICE_MAX_MULTIPLIER
            condition.setType(ProtoOrders.OrderConditionType.ORDER_CONDITION_TYPE_LAST_UP)
        } else {
            price *= PRICE_MIN_MULTIPLIER
            condition.setType(ProtoOrders.OrderConditionType.ORDER_CONDITION_TYPE_LAST_DOWN)
        }
        price = price.toBigDecimal().setScale(scale, RoundingMode.HALF_UP).toDouble()
        condition.setPrice(price)

        return@runBlocking client.createOrder(
            clientId,
            securityBoard,
            securityCode,
            buySell,
            1,
            true,
            property,
            condition.build(),
        )
    }

    fun createLimitOrder(
        client: ClientInterface,
        clientId: String,
        securityBoard: String,
        securityCode: String,
        buySell: ProtoCommon.BuySell
    ) = runBlocking {
        val property = ProtoOrders.OrderProperty.ORDER_PROPERTY_PUT_IN_QUEUE
        val priceScale = getLastClosePriceScale(client, securityBoard, securityCode)
        var price = priceScale[0] as Double
        val scale = priceScale[1] as Int
        price = if (buySell == ProtoCommon.BuySell.BUY_SELL_SELL) {
            price * PRICE_MAX_MULTIPLIER
        } else {
            price * PRICE_MIN_MULTIPLIER
        }
        price = price.toBigDecimal().setScale(scale, RoundingMode.HALF_UP).toDouble()
        return@runBlocking client.createOrder(
            clientId,
            securityBoard,
            securityCode,
            buySell,
            1,
            true,
            property,
            price
        )
    }

    fun createStopLoss(
        client: ClientInterface,
        clientId: String,
        securityBoard: String,
        securityCode: String,
        buySell: ProtoCommon.BuySell,
        linkOrder: Long
    ) = runBlocking {
        val priceScale = getLastClosePriceScale(client, securityBoard, securityCode)
        var price = priceScale[0] as Double
        val scale = priceScale[1] as Int
        val stopLoss = ProtoStops.StopLoss.newBuilder()
        if (buySell == ProtoCommon.BuySell.BUY_SELL_BUY) {
            price *= PRICE_MAX_MULTIPLIER
            price *= PRICE_MAX_MULTIPLIER
        } else {
            price *= PRICE_MIN_MULTIPLIER
            price *= PRICE_MIN_MULTIPLIER
        }
        price = price.toBigDecimal().setScale(scale, RoundingMode.HALF_UP).toDouble()
        stopLoss.setActivationPrice(price)
            .setMarketPrice(true)
            .setQuantity(
                ProtoStops.StopQuantity.newBuilder()
                    .setValue(1.0)
                    .setUnits(
                        ProtoStops.StopQuantityUnits.STOP_QUANTITY_UNITS_LOTS
                    )
                    .build()
            )
            .setUseCredit(true)
        return@runBlocking client.createStopLoss(
            clientId,
            securityBoard,
            securityCode,
            linkOrder,
            buySell,
            stopLoss.build()
        )
    }

    fun createTakeProfit(
        client: ClientInterface,
        clientId: String,
        securityBoard: String,
        securityCode: String,
        buySell: ProtoCommon.BuySell,
        linkOrder: Long
    ) = runBlocking {
        val priceScale = getLastClosePriceScale(client, securityBoard, securityCode)
        var price = priceScale[0] as Double
        val scale = priceScale[1] as Int
        val takeProfit = ProtoStops.TakeProfit.newBuilder()
        if (buySell == ProtoCommon.BuySell.BUY_SELL_BUY) {
            price *= PRICE_MAX_MULTIPLIER
            price *= PRICE_MIN_MULTIPLIER
        } else {
            price *= PRICE_MIN_MULTIPLIER
            price *= PRICE_MAX_MULTIPLIER
        }
        price = price.toBigDecimal().setScale(scale, RoundingMode.HALF_UP).toDouble()
        takeProfit.setActivationPrice(price)
            .setMarketPrice(true)
            .setQuantity(
                ProtoStops.StopQuantity.newBuilder()
                    .setValue(1.0)
                    .setUnits(
                        ProtoStops.StopQuantityUnits.STOP_QUANTITY_UNITS_LOTS
                    )
                    .build()
            )
            .setUseCredit(true)
        return@runBlocking client.createTakeProfit(
            clientId,
            securityBoard,
            securityCode,
            linkOrder,
            buySell,
            takeProfit.build()
        )
    }

    private fun getLastClosePriceScale(
        client: ClientInterface,
        securityBoard: String,
        securityCode: String
    ): List<Any> {
        val candle = getLastMinuteCandle(client, securityBoard, securityCode)
        val priceFinam = candle.getCandles(0).close
        return listOf(priceFinam.num * 10.0.pow((-priceFinam.scale)), priceFinam.scale)
    }

    private fun getLastMinuteCandle(
        client: ClientInterface,
        securityBoard: String,
        securityCode: String
    ) = runBlocking {
        val interval = ProtoCandles.IntradayCandleInterval.newBuilder()
        val now = LocalDateTime.now()
        interval.setTo(
            Timestamp.newBuilder()
                .setSeconds(now.toEpochSecond(ZoneOffset.UTC))
        )
            .setCount(1)
        return@runBlocking client.getCandles(
            securityBoard,
            securityCode,
            ProtoCandles.IntradayCandleTimeFrame.INTRADAYCANDLE_TIMEFRAME_M1,
            interval.build()
        )
    }
}