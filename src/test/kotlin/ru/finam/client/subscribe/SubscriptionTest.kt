package ru.finam.client.subscribe

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import proto.tradeapi.v1.ProtoCommon
import proto.tradeapi.v1.ProtoEvents
import proto.tradeapi.v1.ProtoOrders
import ru.finam.client.Utils
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds


class SubscriptionTest : AbstractTest() {
    private val securityBoard = "TQBR"
    private val securityCode = "VTBR"
    private val orderEvents: MutableList<ProtoEvents.OrderEvent> = mutableListOf()
    private val responseEvents: ConcurrentLinkedQueue<ProtoCommon.ResponseEvent> = ConcurrentLinkedQueue()
    private val tradeEvents: MutableList<ProtoEvents.TradeEvent> = mutableListOf()
    private val orderBookEvents: ConcurrentLinkedQueue<ProtoEvents.OrderBookEvent> = ConcurrentLinkedQueue()


    private fun createLimitOrder(buySell: ProtoCommon.BuySell) =
        Utils.createLimitOrder(client, clientId, securityBoard, securityCode, buySell)


    private fun createConditionalOrder(buySell: ProtoCommon.BuySell) =
        Utils.createConditionalOrder(client, clientId, securityBoard, securityCode, buySell)


    private fun createMarketOrder(buySell: ProtoCommon.BuySell) =
        Utils.createMarketOrder(client, clientId, securityBoard, securityCode, buySell)

    private suspend fun onResponse(event: ProtoCommon.ResponseEvent) {
        if (event.requestId.equals("keepAliveRequest"))
            return
        responseEvents.add(event)
    }

    private suspend fun onOrder(event: ProtoEvents.OrderEvent) {
        orderEvents.add(event)
    }

    private suspend fun onTrade(event: ProtoEvents.TradeEvent) {
        tradeEvents.add(event)
    }

    private suspend fun onOrderBook(event: ProtoEvents.OrderBookEvent) {
        orderBookEvents.add(event)
    }

    init {
        client.onOrder = this::onOrder
        client.onResponse = this::onResponse
        client.onOrderBook = this::onOrderBook
        client.onTrade = this::onTrade
    }

    @ParameterizedTest
    @ValueSource(strings = ["BUY_SELL_SELL", "BUY_SELL_BUY"])
    fun testOrdersSubscribeUnsubscribeWithLimit(buySell: ProtoCommon.BuySell) = runBlocking {
        val requestId = "test_1"

        client.subscribeOrdersTradesAll(requestId, listOf(clientId))

        for (i in 1..6) {
            if (responseEvents.isNotEmpty())
                break
            if (i == 6)
                assertTrue("Нет ответа на запрос подписки") { false }
            delay(1.seconds)
        }
        assertEquals(responseEvents.size, 1)

        for (r in responseEvents) {
            assertEquals(r.requestId, requestId)
            assertTrue { r.success }
        }

        val newOrder = createLimitOrder(buySell)

        for (i in 1..6) {
            if (orderEvents.size == 3)
                break
            if (i == 6)
                assertTrue("Нет ответа на запрос выставления ордера") { false }
            delay(1.seconds)
        }

        for (o in orderEvents) {
            assertEquals(o.transactionId, newOrder.transactionId)
        }
        assertEquals(orderEvents[0].status, ProtoOrders.OrderStatus.ORDER_STATUS_NONE)
        assertEquals(orderEvents[1].status, ProtoOrders.OrderStatus.ORDER_STATUS_ACTIVE)
        assertEquals(orderEvents[2].status, ProtoOrders.OrderStatus.ORDER_STATUS_ACTIVE)
        assertNotNull(orderEvents[2].orderNo)
        orderEvents.clear()

        client.cancelOrder(clientId, newOrder.transactionId)

        for (i in 1..6) {
            if (orderEvents.size == 2)
                break
            if (i == 6)
                assertTrue("Нет ответа на запрос отмены ордера") { false }
            delay(1.seconds)
        }

        for (o in orderEvents) {
            assertEquals(o.transactionId, newOrder.transactionId)
            assertEquals(o.status, ProtoOrders.OrderStatus.ORDER_STATUS_CANCELLED)
        }

        client.unsubscribeOrdersTrades(requestId)

        for (i in 1..6) {
            if (responseEvents.size == 2)
                break
            if (i == 6)
                assertTrue("Нет ответа на запрос отмены подписки") { false }
            delay(1.seconds)
        }

        for (r in responseEvents) {
            assertEquals(r.requestId, requestId)
            assertTrue { r.success }
        }

        orderEvents.clear()
        responseEvents.clear()
    }

    @ParameterizedTest
    @ValueSource(strings = ["BUY_SELL_SELL", "BUY_SELL_BUY"])
    fun testOrdersSubscribeUnsubscribeWithConditional(buySell: ProtoCommon.BuySell) = runBlocking {
        val requestId = "test_2"

        client.subscribeOrdersTradesAll(requestId, listOf(clientId))

        for (i in 1..6) {
            if (responseEvents.isNotEmpty())
                break
            if (i == 6)
                assertTrue("Нет ответа на запрос подписки") { false }
            delay(1.seconds)
        }
        assertEquals(responseEvents.size, 1)

        for (r in responseEvents) {
            assertEquals(r.requestId, requestId)
            assertTrue { r.success }
        }

        val newOrder = createConditionalOrder(buySell)

        for (i in 1..6) {
            if (orderEvents.size == 2)
                break
            if (i == 6)
                assertTrue("Нет ответа на запрос выставления ордера") { false }
            delay(1.seconds)
        }

        for (o in orderEvents) {
            assertEquals(o.transactionId, newOrder.transactionId)
            assertEquals(o.status, ProtoOrders.OrderStatus.ORDER_STATUS_ACTIVE)
        }

        orderEvents.clear()

        client.cancelOrder(clientId, newOrder.transactionId)
        for (i in 1..6) {
            if (orderEvents.size == 1)
                break
            if (i == 6)
                assertTrue("Нет ответа на запрос отмены ордера") { false }
            delay(1.seconds)
        }

        for (o in orderEvents) {
            assertEquals(o.transactionId, newOrder.transactionId)
            assertEquals(o.status, ProtoOrders.OrderStatus.ORDER_STATUS_CANCELLED)
        }

        client.unsubscribeOrdersTrades(requestId)
        for (i in 1..6) {
            if (responseEvents.size == 2)
                break
            if (i == 6)
                assertTrue("Нет ответа на запрос отмены подписки") { false }
            delay(1.seconds)
        }

        for (r in responseEvents) {
            assertEquals(r.requestId, requestId)
            assertTrue { r.success }
        }

        orderEvents.clear()
        responseEvents.clear()
    }

    @Test
    fun testOrderBookOneSubscribeUnsubscribe() = runBlocking {
        val requestId = "test_3"
        client.subscribeOrderBook(requestId, securityBoard, securityCode)

        for (i in 1..6) {
            if (responseEvents.isNotEmpty())
                break
            if (i == 6)
                assertTrue("Нет ответа на запрос подписки") { false }
            delay(1.seconds)
        }
        for (r in responseEvents) {
            assertEquals(r.requestId, requestId)
            assertTrue { r.success }
        }
        delay(5.seconds)
        assertTrue { orderBookEvents.isNotEmpty() }
        for (ob in orderBookEvents) {
            assertEquals(ob.securityBoard, securityBoard)
            assertEquals(ob.securityCode, securityCode)
        }

        client.unsubscribeOrderBook(requestId, securityBoard, securityCode)
        for (i in 1..6) {
            if (responseEvents.size == 2)
                break
            if (i == 6)
                assertTrue("Нет ответа на запрос отмены подписки") { false }
            delay(1.seconds)
        }
        for (r in responseEvents) {
            assertEquals(r.requestId, requestId)
            assertTrue { r.success }
        }

        orderBookEvents.clear()
        responseEvents.clear()
        delay(2.seconds)
        assertEquals(orderBookEvents.size, 0)
    }

    @Test
    fun testOrderBookSeveralSubscribeUnsubscribe() = runBlocking {
        val requestId = "test_4.1"
        val requestId1 = "test_4.2"
        val securityCode1 = "SBER"

        launch {
            client.subscribeOrderBook(requestId, securityBoard, securityCode)
            client.subscribeOrderBook(requestId1, securityBoard, securityCode1)
        }

        for (i in 1..6) {
            if (responseEvents.size == 2)
                break
            if (i == 6)
                assertEquals(2, responseEvents.size)
            delay(1.seconds)
        }
        var checkVtb = false
        var checkSber = false
        for (r in responseEvents) {
            if (r.requestId.equals(requestId))
                checkVtb = true
            else if (r.requestId.equals(requestId1))
                checkSber = true
            assertTrue { r.success }
        }
        assertTrue("Нет ответа на запрос подписки $requestId1") { checkSber }
        assertTrue("Нет ответа на запрос подписки $requestId") { checkVtb }
        delay(5.seconds)

        checkVtb = false
        checkSber = false
        assertTrue { orderBookEvents.isNotEmpty() }
        for (ob in orderBookEvents) {
            assertEquals(ob.securityBoard, securityBoard)
            if (ob.securityCode.equals(securityCode))
                checkVtb = true
            else if (ob.securityCode.equals(securityCode1))
                checkSber = true
        }
        assertTrue("Нет событий стакана $requestId1") { checkSber }
        assertTrue("Нет событий стакана $requestId") { checkVtb }

        responseEvents.clear()

        launch {
            client.unsubscribeOrderBook(requestId, securityBoard, securityCode)
            client.unsubscribeOrderBook(requestId1, securityBoard, securityCode1)
        }

        for (i in 1..6) {
            if (responseEvents.size == 2)
                break
            if (i == 6)
                assertTrue("Нет ответа на запрос отмены подписки") { false }
            delay(1.seconds)
        }

        checkVtb = false
        checkSber = false
        for (r in responseEvents) {
            if (r.requestId.equals(requestId))
                checkVtb = true
            else if (r.requestId.equals(requestId1))
                checkSber = true
            assertTrue { r.success }
        }
        assertTrue("Нет ответа на запрос отмены подписки $requestId1") { checkSber }
        assertTrue("Нет ответа на запрос отмены подписки $requestId") { checkVtb }

        orderBookEvents.clear()
        responseEvents.clear()
        delay(2.seconds)
        assertEquals(orderBookEvents.size, 0)
    }

    @Test
    fun testTradesSubscribeUnsubscribe() = runBlocking {
        val requestId = "test_5"

        client.subscribeOrdersTradesAll(requestId, listOf(clientId))

        for (i in 1..6) {
            if (responseEvents.size == 1)
                break
            if (i == 6)
                assertEquals(1, responseEvents.size)
            delay(1.seconds)
        }
        for (r in responseEvents) {
            assertEquals(r.requestId, requestId)
            assertTrue { r.success }
        }

        createMarketOrder(
            ProtoCommon.BuySell.BUY_SELL_BUY
        )

        for (i in 1..6) {
            if (orderEvents.size == 3)
                break
            if (i == 6)
                assertTrue("Нет ответа на запрос выставления ордера") { false }
            delay(1.seconds)
        }
        assertEquals(orderEvents[0].status, ProtoOrders.OrderStatus.ORDER_STATUS_NONE)
        assertEquals(orderEvents[1].status, ProtoOrders.OrderStatus.ORDER_STATUS_ACTIVE)
        assertEquals(orderEvents[2].status, ProtoOrders.OrderStatus.ORDER_STATUS_MATCHED)

        var orderNo = orderEvents[2].orderNo
        orderEvents.clear()


        for (t in tradeEvents) {
            assertEquals(t.orderNo, orderNo)
            assertEquals(t.securityCode, securityCode)
            assertEquals(t.buySell, ProtoCommon.BuySell.BUY_SELL_BUY)
        }
        tradeEvents.clear()

        createMarketOrder(
            ProtoCommon.BuySell.BUY_SELL_SELL
        )

        for (i in 1..6) {
            if (orderEvents.size == 3)
                break
            if (i == 6)
                assertTrue("Нет ответа на запрос выставления ордера") { false }
            delay(1.seconds)
        }
        assertEquals(orderEvents[0].status, ProtoOrders.OrderStatus.ORDER_STATUS_NONE)
        assertEquals(orderEvents[1].status, ProtoOrders.OrderStatus.ORDER_STATUS_ACTIVE)
        assertEquals(orderEvents[2].status, ProtoOrders.OrderStatus.ORDER_STATUS_MATCHED)

        orderNo = orderEvents[2].orderNo
        for (t in tradeEvents) {
            assertEquals(t.orderNo, orderNo)
            assertEquals(t.securityCode, securityCode)
            assertEquals(t.buySell, ProtoCommon.BuySell.BUY_SELL_SELL)
        }

        client.unsubscribeOrdersTrades(requestId)

        for (i in 1..6) {
            if (responseEvents.size == 2)
                break
            if (i == 6)
                assertEquals(2, responseEvents.size)
            delay(1.seconds)
        }
        for (r in responseEvents) {
            assertEquals(r.requestId, requestId)
            assertTrue { r.success }
        }

        responseEvents.clear()
        orderEvents.clear()
        tradeEvents.clear()
    }
}