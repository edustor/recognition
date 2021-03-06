package ru.edustor.recognition.internal

import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.amqp.rabbit.core.RabbitTemplate
import ru.edustor.commons.models.rabbit.processing.pages.PdfUploadedEvent
import ru.edustor.commons.storage.service.BinaryObjectStorageService
import ru.edustor.commons.storage.service.BinaryObjectStorageService.ObjectType.PDF_UPLOAD
import ru.edustor.recognition.rabbit.RabbitHandler
import java.time.Instant

class RabbitTests {
    @Test
    fun testRabbit() {
        val uploadedEvent = PdfUploadedEvent(
                uuid = "3559c71e-a9ea-4493-883f-ee080047bb1b",
                userId = "844fc749-1efb-448e-99a9-51e5a4eb2e4f",
                timestamp = Instant.now(),
                targetLessonId = null)

        val pdfStream = javaClass.getResource("/generated.pdf").openStream()

        val storageServiceMock = mock(BinaryObjectStorageService::class.java)
        `when`(storageServiceMock.get(PDF_UPLOAD, uploadedEvent.uuid)).thenReturn(pdfStream)

        val rabbitMock = mock(RabbitTemplate::class.java)

        val rabbitHandler = RabbitHandler(storageServiceMock, rabbitMock)

        rabbitHandler.processFile(uploadedEvent)

//        TODO: Assert result

//        Assertions.assertEquals(uploadedEvent.uuid, result.uuid)
//        Assertions.assertEquals(uploadedEvent.userId, result.userId)
//        Assertions.assertEquals(0, result.pagesList[0].index)
//        Assertions.assertEquals(1, result.pagesList[1].index)
//        Assertions.assertEquals("da5ba7c5-7b26-48b9-8cc9-44a8bc92da9f", result.pagesList[0].qrUuid)
//        Assertions.assertEquals("e3d16e38-1bbf-4060-91a0-52801f56fbdd", result.pagesList[1].qrUuid)
    }
}