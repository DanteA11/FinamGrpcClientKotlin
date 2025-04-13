package ru.finam.client.defaultclient

import com.google.protobuf.Timestamp
import com.google.type.Date
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import proto.tradeapi.v1.ProtoCandles
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals


class GetRequestsTest : AbstractTest() {

    @ParameterizedTest
    @CsvSource(
        "SBER, DAYCANDLE_TIMEFRAME_D1, 1",
        "SBER, DAYCANDLE_TIMEFRAME_W1, 2",
        "VTBR, DAYCANDLE_TIMEFRAME_D1, 5",
        "VTBR, DAYCANDLE_TIMEFRAME_W1, 10"
    )
    fun testGetDayCandlesWithTo(securityCode: String, timeFrame: ProtoCandles.DayCandleTimeFrame, count: Int) =
        runBlocking {
            val interval = ProtoCandles.DayCandleInterval.newBuilder()
            val now = LocalDate.now()
            interval.setTo(
                Date.newBuilder()
                    .setDay(now.dayOfMonth)
                    .setMonth(now.monthValue)
                    .setYear(now.year)
            )
                .setCount(count)
            val result = client.getCandles("TQBR", securityCode, timeFrame, interval.build())
            assertEquals(result.candlesCount, count)
        }

    @ParameterizedTest
    @CsvSource(
        "SBER, DAYCANDLE_TIMEFRAME_D1, 1",
        "SBER, DAYCANDLE_TIMEFRAME_W1, 2",
        "VTBR, DAYCANDLE_TIMEFRAME_D1, 5",
        "VTBR, DAYCANDLE_TIMEFRAME_W1, 10"
    )
    fun testGetDayCandlesWithFrom(securityCode: String, timeFrame: ProtoCandles.DayCandleTimeFrame, count: Int) =
        runBlocking {
            val interval = ProtoCandles.DayCandleInterval.newBuilder()
            val date = LocalDate.now().minusDays(80)

            interval.setFrom(
                Date.newBuilder()
                    .setDay(date.dayOfMonth)
                    .setMonth(date.monthValue)
                    .setYear(date.year)
            )
                .setCount(count)

            val result = client.getCandles("TQBR", securityCode, timeFrame, interval.build())

            assertEquals(result.candlesCount, count)
        }

    @ParameterizedTest
    @CsvSource(
        "SBER, INTRADAYCANDLE_TIMEFRAME_M1, 1",
        "SBER, INTRADAYCANDLE_TIMEFRAME_M5, 26",
        "SBER, INTRADAYCANDLE_TIMEFRAME_M15, 10",
        "SBER, INTRADAYCANDLE_TIMEFRAME_H1, 44",
        "VTBR, INTRADAYCANDLE_TIMEFRAME_M1, 5",
        "VTBR, INTRADAYCANDLE_TIMEFRAME_M5, 3",
        "VTBR, INTRADAYCANDLE_TIMEFRAME_M15, 12",
        "VTBR, INTRADAYCANDLE_TIMEFRAME_H1, 8"
    )
    fun testGetIntraDayCandlesWithTo(
        securityCode: String,
        timeFrame: ProtoCandles.IntradayCandleTimeFrame,
        count: Int
    ) = runBlocking {
        val interval = ProtoCandles.IntradayCandleInterval.newBuilder()
        val now = LocalDateTime.now()
        interval.setTo(
            Timestamp.newBuilder()
                .setSeconds(now.toEpochSecond(ZoneOffset.UTC))
        )
            .setCount(count)

        val result = client.getCandles("TQBR", securityCode, timeFrame, interval.build())

        assertEquals(result.candlesCount, count)
    }

    @ParameterizedTest
    @CsvSource(
        "SBER, INTRADAYCANDLE_TIMEFRAME_M1, 1",
        "SBER, INTRADAYCANDLE_TIMEFRAME_M5, 26",
        "SBER, INTRADAYCANDLE_TIMEFRAME_M15, 10",
        "SBER, INTRADAYCANDLE_TIMEFRAME_H1, 44",
        "VTBR, INTRADAYCANDLE_TIMEFRAME_M1, 5",
        "VTBR, INTRADAYCANDLE_TIMEFRAME_M5, 3",
        "VTBR, INTRADAYCANDLE_TIMEFRAME_M15, 12",
        "VTBR, INTRADAYCANDLE_TIMEFRAME_H1, 8"
    )
    fun testGetIntraDayCandlesWithFrom(
        securityCode: String,
        timeFrame: ProtoCandles.IntradayCandleTimeFrame,
        count: Int
    ) = runBlocking {
        val interval = ProtoCandles.IntradayCandleInterval.newBuilder()
        val date = LocalDateTime.now().minusDays(10)

        interval.setFrom(
            Timestamp.newBuilder()
                .setSeconds(date.toEpochSecond(ZoneOffset.UTC))
        )
            .setCount(count)

        val result = client.getCandles("TQBR", securityCode, timeFrame, interval.build())

        assertEquals(result.candlesCount, count)
    }

    @ParameterizedTest
    @CsvSource(
        "true, true, true, true",
        "false, false, false, false",
        "true, false, true, false",
        "false, true, false, true",
        "true, false, false, true"
    )
    fun testGetPortfolio(
        includeCurrencies: Boolean,
        includeMoney: Boolean,
        includePositions: Boolean,
        includeMaxBuySell: Boolean
    ) = runBlocking {
        val result = client.getPortfolio(clientId, includeCurrencies, includeMoney, includePositions, includeMaxBuySell)
        assertEquals(result.clientId, clientId)
        assertEquals(result.content.includeCurrencies, includeCurrencies)
        assertEquals(result.content.includeMoney, includeMoney)
        assertEquals(result.content.includePositions, includePositions)
        assertEquals(result.content.includeMaxBuySell, includeMaxBuySell)
    }

    @ParameterizedTest
    @CsvSource(
        "true, true, true",
        "false, false, false",
        "true, false, true",
        "false, true, false"
    )
    fun testGetOrders(
        includeActive: Boolean,
        includeCanceled: Boolean,
        includeMatched: Boolean
    ) = runBlocking {
        val result = client.getOrders(clientId, includeActive, includeCanceled, includeMatched)
        assertEquals(result.clientId, clientId)
    }

    @ParameterizedTest
    @CsvSource(
        "true, true, true",
        "false, false, false",
        "true, false, true",
        "false, true, false"
    )
    fun testGetStops(
        includeActive: Boolean,
        includeCanceled: Boolean,
        includeExecuted: Boolean,
    ) = runBlocking {
        val result = client.getStops(clientId, includeActive, includeCanceled, includeExecuted)
        assertEquals(result.clientId, clientId)
    }

    @Test
    fun testGetSecurities() = runBlocking {
        val securityBoard = ""
        val securityCode = "SBER"
        val result = client.getSecurities(securityBoard, securityCode)
        for (security in result.securitiesList)
            assertEquals(security.code, securityCode)
    }
}