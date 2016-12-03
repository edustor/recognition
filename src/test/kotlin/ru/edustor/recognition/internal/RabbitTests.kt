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

class RabbitTests {
    @Test
    fun testRabbit() {
        val uploadedEvent = PdfUploadedEvent.newBuilder()
                .setUserId("844fc749-1efb-448e-99a9-51e5a4eb2e4f")
                .setUuid("3559c71e-a9ea-4493-883f-ee080047bb1b")
                .setTimestamp(Instant.now().epochSecond)
                .build()

        val pdfStream = javaClass.getResource("/generated.pdf").openStream()

        val storageServiceMock = mock(PdfStorageService::class.java)
        `when`(storageServiceMock.getUploadedPdf(uploadedEvent.uuid)).thenReturn(pdfStream)

        val rabbitHandler = RabbitHandler(storageServiceMock)

        val resultBytes = rabbitHandler.processFile(uploadedEvent.toByteArray())
        val result = PdfRecognizedEvent.parseFrom(resultBytes)

        Assertions.assertEquals(uploadedEvent.uuid, result.uuid)
        Assertions.assertEquals(uploadedEvent.userId, result.userId)
        Assertions.assertEquals(0, result.pagesList[0].index)
        Assertions.assertEquals(1, result.pagesList[1].index)
        Assertions.assertEquals("da5ba7c5-7b26-48b9-8cc9-44a8bc92da9f", result.pagesList[0].qrUuid)
        Assertions.assertEquals("e3d16e38-1bbf-4060-91a0-52801f56fbdd", result.pagesList[1].qrUuid)
    }
}