package ru.edustor.recognition.rabbit

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import ru.edustor.commons.models.internal.processing.pdf.PageRecognizedEvent
import ru.edustor.commons.models.internal.processing.pdf.PdfUploadedEvent
import ru.edustor.commons.storage.service.BinaryObjectStorageService
import ru.edustor.commons.storage.service.BinaryObjectStorageService.ObjectType
import ru.edustor.recognition.exception.PdfNotFoundException
import ru.edustor.recognition.internal.PageExtractor
import ru.edustor.recognition.internal.PdfRenderer
import ru.edustor.recognition.internal.QrReader
import java.util.*

@Component
open class RabbitHandler(var storage: BinaryObjectStorageService, val rabbitTemplate: RabbitTemplate) {
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
    fun processFile(event: PdfUploadedEvent) {
        logger.info("Processing file ${event.uuid} uploaded by ${event.userId}")

        val uploadedPdfStream = storage.get(ObjectType.PDF_UPLOAD, event.uuid)
            ?: throw PdfNotFoundException("Failed to find file with id ${event.uuid}")

        val pageExtractor = PageExtractor(uploadedPdfStream)

        var i = 0
        pageExtractor.forEach { pageBytes ->
            val renderer = PdfRenderer(pageBytes.inputStream())
            val renderedPage = renderer.next()

            val qrReader = QrReader()

            val qrData = qrReader.read(renderedPage)
            val qrUuid = qrData?.let { UUID_REGEX.find(qrData)?.value }

            val pageUuid = UUID.randomUUID().toString()

            storage.put(ObjectType.PAGE, pageUuid, pageBytes.inputStream(), pageBytes.size.toLong())

            val recognizedEvent = PageRecognizedEvent(event.uuid,
                    pageUuid,
                    event.userId,
                    i++,
                    qrUuid,
                    event.targetLessonId,
                    event.timestamp)
            rabbitTemplate.convertAndSend("internal.edustor", "recognized.pages.processing", recognizedEvent)

            logger.info("Processed page $i ($pageUuid). QR: $qrData")
        }

        storage.delete(ObjectType.PDF_UPLOAD, event.uuid)

        logger.info("File processing finished: ${event.uuid}")
    }
}