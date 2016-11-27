package ru.edustor.recognition.internal

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import ru.edustor.proto.EdustorPdfProcessingProtos.PdfRecognizedEvent
import ru.edustor.proto.EdustorPdfProcessingProtos.PdfUploadedEvent
import ru.edustor.recognition.rabbit.RabbitHandler
import ru.edustor.recognition.service.PdfStorageService
import java.time.Instant

class TestRabbit {
    @Test
    fun testRabbit() {
        val uploadedEvent = PdfUploadedEvent.newBuilder()
                .setUserId("844fc749-1efb-448e-99a9-51e5a4eb2e4f")
                .setUuid("3559c71e-a9ea-4493-883f-ee080047bb1b")
                .setTimestamp(Instant.now().epochSecond)
                .build()

        val pdfStream = javaClass.getResource("/scanned.pdf").openStream()

        val storageServiceMock = mock(PdfStorageService::class.java)
        `when`(storageServiceMock.getUploadedPdf(uploadedEvent.uuid)).thenReturn(pdfStream)

        val rabbitHandler = RabbitHandler(storageServiceMock)

        val resultBytes = rabbitHandler.processFile(uploadedEvent.toByteArray())
        val result = PdfRecognizedEvent.parseFrom(resultBytes)

        Assertions.assertEquals(uploadedEvent.uuid, result.uuid)
        Assertions.assertEquals(uploadedEvent.userId, result.userId)
        Assertions.assertEquals(0, result.pagesList[0].index)
        Assertions.assertEquals("a5b9730a-ef08-4a9d-8950-c37d8d661f4b", result.pagesList[0].qrUuid)
    }
}