package ru.edustor.recognition.rabbit

import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.stereotype.Component
import ru.edustor.proto.EdustorPdfProcessingProtos.PdfRecognizedEvent
import ru.edustor.proto.EdustorPdfProcessingProtos.PdfUploadedEvent
import ru.edustor.recognition.internal.PdfRenderer
import ru.edustor.recognition.internal.QrReader
import ru.edustor.recognition.service.PdfStorageService

@Component
open class RabbitHandler(var storage: PdfStorageService) {
    @RabbitListener(bindings = arrayOf(QueueBinding(
            value = Queue("recognition.edustor.ru/incoming", durable = "true", arguments = arrayOf(
                    Argument(name = "x-dead-letter-exchange", value = "reject.edustor.ru")
            )),
            exchange = Exchange("internal.edustor.ru", type = ExchangeTypes.TOPIC,
                    ignoreDeclarationExceptions = "true",
                    durable = "true"),
            key = "uploaded.pdf.events"
    )))
    fun processFile(msg: ByteArray): ByteArray {
        val event = PdfUploadedEvent.parseFrom(msg)

        val uploadedPdfStream = storage.getUploadedPdf(event.uuid)
        val renderer = PdfRenderer(uploadedPdfStream)
        val qrReader = QrReader()

        var i = 0
        val pages: MutableList<PdfRecognizedEvent.PdfPage> = mutableListOf()
        renderer.forEach { renderedPage ->
            val qrUuid = qrReader.read(renderedPage)
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

        return result.toByteArray()
    }
}