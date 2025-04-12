package ru.finam

import com.google.protobuf.Timestamp
import kotlinx.coroutines.*
import proto.tradeapi.v1.ProtoCandles
import proto.tradeapi.v1.ProtoCandles.IntradayCandleInterval
import ru.finam.ClientHolder.getSubscribeClient
import ru.finam.ClientHolder.getDefaultClient

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

fun main() = runBlocking {
    System.setProperty("slf4j.internal.verbosity", "WARN");
//    val client = getSubscribeClient("CAEQ/6/rBRoY8dq3kATAY8MeTODiLoZEZZ5gyqY4ckq7")
//    val i = IntradayCandleInterval.newBuilder()
//    i.setTo(Timestamp.newBuilder().setSeconds(java.util.Date().time))
//        .setCount(1)
//    val interval = i.build()
//
//    val res = client.getCandles(
//            "TQBR",
//            "SBER",
//            ProtoCandles.IntradayCandleTimeFrame.INTRADAYCANDLE_TIMEFRAME_M1,
//            interval
//        )
//
//    val res1 = client.getCandles(
//            "TQBR",
//            "VTBR",
//            ProtoCandles.IntradayCandleTimeFrame.INTRADAYCANDLE_TIMEFRAME_M1,
//            interval
//        )
//    delay(150000)
//    client.stop()
//    val token = System.getenv("FINAM_USER_TOKEN")
//    println(token)
//    println(res)
    println(listOf("123")[0])
}

