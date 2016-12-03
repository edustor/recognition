package ru.edustor.recognition.rabbit

import com.google.protobuf.InvalidProtocolBufferException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Component
import ru.edustor.proto.EdustorPdfProcessingProtos.PdfRecognizedEvent
import ru.edustor.proto.EdustorPdfProcessingProtos.PdfUploadedEvent
import ru.edustor.recognition.internal.PdfRenderer
import ru.edustor.recognition.internal.QrReader
import ru.edustor.recognition.service.PdfStorageService

@Component
open class RabbitHandler(var storage: PdfStorageService) {
    val UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".toRegex()
    val logger: Logger = LoggerFactory.getLogger(RabbitHandler::class.java)

    @RabbitListener(bindings = arrayOf(QueueBinding(
            value = Queue("recognition.edustor.ru/incoming", durable = "true", arguments = arrayOf(
                    Argument(name = "x-dead-letter-exchange", value = "reject.edustor.ru")
            )),
            exchange = Exchange("internal.edustor.ru", type = ExchangeTypes.TOPIC,
                    ignoreDeclarationExceptions = "true",
                    durable = "true"),
            key = "uploaded.pdf.event"
    )))
    @SendTo("internal.edustor.ru/recognized.pdf.event")
    fun processFile(msg: ByteArray): ByteArray {
        val event: PdfUploadedEvent
        try {
            event = PdfUploadedEvent.parseFrom(msg)
        } catch (e: InvalidProtocolBufferException) {
            throw AmqpRejectAndDontRequeueException(e)
        }

        logger.info("Processing file ${event.uuid} uploaded by ${event.userId}")

        val uploadedPdfStream = storage.getUploadedPdf(event.uuid)
        val renderer = PdfRenderer(uploadedPdfStream)
        val qrReader = QrReader()

        var i = 0
        val pages: MutableList<PdfRecognizedEvent.PdfPage> = mutableListOf()
        renderer.forEach { renderedPage ->
            val qrData = qrReader.read(renderedPage)
            val qrUuid = qrData?.let { UUID_REGEX.find(qrData)?.value }

            pages.add(PdfRecognizedEvent.PdfPage.newBuilder()
                    .setIndex(i++)
                    .setQrUuid(qrUuid)
                    .build()
            )
        }

        val result = PdfRecognizedEvent.newBuilder()
                .setUserId(event.userId)
                .setUuid(event.uuid)
                .addAllPages(pages)
                .build()

        logger.info("Successfully finished")

        return result.toByteArray()
    }
}