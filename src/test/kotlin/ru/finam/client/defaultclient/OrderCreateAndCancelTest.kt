package ru.finam.client.defaultclient

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import proto.tradeapi.v1.ProtoCommon
import proto.tradeapi.v1.ProtoOrders
import proto.tradeapi.v1.ProtoStops
import ru.finam.client.Utils
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class OrderCreateAndCancelTest: AbstractTest() {
    private val securityBoard = "TQBR"
    private val securityCode = "VTBR"

    private fun createConditionalOrder(buySell: ProtoCommon.BuySell): ProtoOrders.NewOrderResult {
        return Utils.createConditionalOrder(client, clientId, securityBoard, securityCode, buySell)
    }

    private fun createLimitOrder(buySell: ProtoCommon.BuySell): ProtoOrders.NewOrderResult {
        return Utils.createLimitOrder(client, clientId, securityBoard, securityCode, buySell)
    }

    private fun createStopLoss(buySell: ProtoCommon.BuySell, linkOrder: Long): ProtoStops.NewStopResult {
        return Utils.createStopLoss(client, clientId, securityBoard, securityCode, buySell, linkOrder)
    }

    private fun createTakeProfit(buySell: ProtoCommon.BuySell, linkOrder: Long): ProtoStops.NewStopResult {
        return Utils.createTakeProfit(client, clientId, securityBoard, securityCode, buySell, linkOrder)
    }

    @ParameterizedTest
    @ValueSource(strings = ["BUY_SELL_SELL", "BUY_SELL_BUY"])
    fun testCreateAndCancelConditionalOrder(buySell: ProtoCommon.BuySell) = runBlocking {
        var orders = client.getActiveOrders(clientId)
        val amount = orders.ordersCount
        val order = createConditionalOrder(buySell)
        assertEquals(order.clientId, clientId)
        delay(1.seconds)
        orders = client.getActiveOrders(clientId)
        assertEquals(orders.ordersCount, amount + 1)
        val cancelOrder = client.cancelOrder(clientId, order.transactionId)
        assertEquals(cancelOrder.clientId, clientId)
        delay(1.seconds)
        orders = client.getActiveOrders(clientId)
        assertEquals(orders.ordersCount, amount)
    }

    @ParameterizedTest
    @ValueSource(strings = ["BUY_SELL_SELL", "BUY_SELL_BUY"])
    fun testCreateAndCancelLimitOrder(buySell: ProtoCommon.BuySell) = runBlocking {
        var orders = client.getActiveOrders(clientId)
        val amount = orders.ordersCount
        val order = createLimitOrder(buySell)
        assertEquals(order.clientId, clientId)
        delay(1.seconds)
        orders = client.getActiveOrders(clientId)
        assertEquals(orders.ordersCount, amount + 1)
        val cancelOrder = client.cancelOrder(clientId, order.transactionId)
        assertEquals(cancelOrder.clientId, clientId)
        delay(1.seconds)
        orders = client.getActiveOrders(clientId)
        assertEquals(orders.ordersCount, amount)
    }

    @ParameterizedTest
    @ValueSource(strings = ["BUY_SELL_SELL", "BUY_SELL_BUY"])
    fun testCreateAndCancelCreateStopLoss(buySell : ProtoCommon.BuySell) = runBlocking {
        var stops = client.getActiveStops(clientId)
        val amount = stops.stopsCount
        val order = if (buySell == ProtoCommon.BuySell.BUY_SELL_SELL)
            createLimitOrder(ProtoCommon.BuySell.BUY_SELL_BUY)
        else
            createLimitOrder(ProtoCommon.BuySell.BUY_SELL_SELL)
        delay(1.seconds)
        var orders = client.getActiveOrders(clientId)
        val ordersAmount = orders.ordersCount
        val orderNo = orders.getOrders(0).orderNo
        val stop = createStopLoss(buySell, orderNo)
        delay(1.seconds)
        stops = client.getActiveStops(clientId)
        assertEquals(stops.stopsCount, amount + 1)
        val cancelStop = client.cancelStops(clientId, stop.stopId)
        assertEquals(cancelStop.clientId, clientId)
        assertEquals(cancelStop.stopId, stop.stopId)
        delay(1.seconds)
        stops = client.getActiveStops(clientId)
        assertEquals(stops.stopsCount, amount)
        val cancelOrder = client.cancelOrder(clientId, order.transactionId)
        assertEquals(cancelOrder.clientId, clientId)
        assertEquals(cancelOrder.transactionId, order.transactionId)
        delay(1.seconds)
        orders = client.getActiveOrders(clientId)
        assertEquals(orders.ordersCount, ordersAmount - 1)
    }


    @ParameterizedTest
    @ValueSource(strings = ["BUY_SELL_SELL", "BUY_SELL_BUY"])
    fun testCreateAndCancelTakeProfit(buySell : ProtoCommon.BuySell) = runBlocking {
        var stops = client.getActiveStops(clientId)
        val amount = stops.stopsCount
        val order = if (buySell == ProtoCommon.BuySell.BUY_SELL_SELL)
            createLimitOrder(ProtoCommon.BuySell.BUY_SELL_BUY)
        else
            createLimitOrder(ProtoCommon.BuySell.BUY_SELL_SELL)
        delay(1.seconds)
        var orders = client.getActiveOrders(clientId)
        val ordersAmount = orders.ordersCount
        val orderNo = orders.getOrders(0).orderNo
        val take = createTakeProfit(buySell, orderNo)
        delay(1.seconds)
        stops = client.getActiveStops(clientId)
        assertEquals(stops.stopsCount, amount + 1)
        val cancelStop = client.cancelStops(clientId, take.stopId)
        assertEquals(cancelStop.clientId, clientId)
        assertEquals(cancelStop.stopId, take.stopId)
        delay(1.seconds)
        stops = client.getActiveStops(clientId)
        assertEquals(stops.stopsCount, amount)
        val cancelOrder = client.cancelOrder(clientId, order.transactionId)
        assertEquals(cancelOrder.clientId, clientId)
        assertEquals(cancelOrder.transactionId, order.transactionId)
        delay(1.seconds)
        orders = client.getActiveOrders(clientId)
        assertEquals(orders.ordersCount, ordersAmount - 1)
    }

}