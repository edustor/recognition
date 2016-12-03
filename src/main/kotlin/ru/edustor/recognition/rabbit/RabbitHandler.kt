package ru.edustor.recognition.rabbit

import com.google.protobuf.InvalidProtocolBufferException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import ru.edustor.proto.internal.EdustorPdfProcessingProtos.PageRecognizedEvent
import ru.edustor.proto.internal.EdustorPdfProcessingProtos.PdfUploadedEvent
import ru.edustor.recognition.internal.PdfRenderer
import ru.edustor.recognition.internal.QrReader
import ru.edustor.recognition.service.PdfStorageService
import java.util.*

@Component
open class RabbitHandler(var storage: PdfStorageService, val rabbitTemplate: RabbitTemplate) {
    val UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".toRegex()
    val logger: Logger = LoggerFactory.getLogger(RabbitHandler::class.java)

    @RabbitListener(bindings = arrayOf(QueueBinding(
            value = Queue("recognition.edustor/inbox", durable = "true", arguments = arrayOf(
                    Argument(name = "x-dead-letter-exchange", value = "reject.edustor")
            )),
            exchange = Exchange("internal.edustor", type = ExchangeTypes.TOPIC,
                    ignoreDeclarationExceptions = "true",
                    durable = "true"),
            key = "uploaded.pdf.pages.processing"
    )))
    fun processFile(msg: ByteArray) {
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
        renderer.forEach { renderedPage ->
            val qrData = qrReader.read(renderedPage)
            val qrUuid = qrData?.let { UUID_REGEX.find(qrData)?.value }

            val pageUuid = UUID.randomUUID().toString()

            val recognizedEvent = PageRecognizedEvent.newBuilder()
                    .setUploadUuid(event.uuid)
                    .setPageIndex(i++)
                    .setPageUuid(pageUuid)
                    .setQrUuid(qrUuid)
                    .setUserId(event.userId)
                    .build()

            logger.info("Processed page $i ($pageUuid). QR: $qrData")

            rabbitTemplate.convertAndSend("internal.edustor", "recognized.pages.processing", recognizedEvent.toByteArray())
        }

        logger.info("Successfully finished")
    }
}