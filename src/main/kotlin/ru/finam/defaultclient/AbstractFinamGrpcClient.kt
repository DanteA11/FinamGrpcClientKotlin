package ru.finam.defaultclient

import com.google.protobuf.GeneratedMessageV3
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.StatusException

abstract class AbstractFinamGrpcClient(
    token: String,
    url: String = "trade-api.finam.ru"
) : ClientInterface {
    open val logger = KotlinLogging.logger {}


    protected val channel: ManagedChannel = ManagedChannelBuilder.forTarget(url).build()
    protected val metadata: Metadata = getMetadata(token)

    private fun getMetadata(token: String): Metadata {
        val metadata = Metadata()
        val key = Metadata.Key.of(
            "x-api-key", Metadata.ASCII_STRING_MARSHALLER
        )
        metadata.put(key, token)
        return metadata
    }

    override fun stop() {
        channel.shutdown()
        logger.info { "Канал закрыт" }
    }

    /**
     * Выполняет запрос к Api.
     * В случае ошибки выполняет stop().
     *
     * @param request Модель запроса
     * @param func Функция для выполнения
     * @return Результат выполнения запроса
     */
    protected suspend fun <Req : GeneratedMessageV3, Res : GeneratedMessageV3> executeRequest(
        request: Req, func: suspend (Req, Metadata) -> Res
    ): Res {
        try {
            return func(request, metadata)
        } catch (e: StatusException) {
            stop()
            throw e
        }
    }
}