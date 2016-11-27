package ru.edustor.recognition.internal

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.edustor.proto.EdustorPdfProcessingProtos.PdfRecognizedEvent
import ru.edustor.proto.EdustorPdfProcessingProtos.PdfUploadedEvent
import ru.edustor.recognition.rabbit.RabbitHandler
import java.time.Instant

class TestRabbit {
    @Test
    fun testRabbit() {
        val rabbitHandler = RabbitHandler()

        val uploadedEvent = PdfUploadedEvent.newBuilder()
                .setUserId("844fc749-1efb-448e-99a9-51e5a4eb2e4f")
                .setUuid("3559c71e-a9ea-4493-883f-ee080047bb1b")
                .setTimestamp(Instant.now().epochSecond)
                .build()

        val resultBytes = rabbitHandler.processFile(uploadedEvent.toByteArray())
        val result = PdfRecognizedEvent.parseFrom(resultBytes)

        Assertions.assertEquals(uploadedEvent.uuid, result.uuid)
        Assertions.assertEquals(uploadedEvent.userId, result.userId)
    }
}