package ru.edustor.recognition.pages

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO

class TestQR {
    @Test
    fun qr() {
        val image = ImageIO.read(javaClass.getResource("/zxing_test.png"))
        val qrReader = QrReader()

        val result = qrReader.read(image)

        Assertions.assertNotNull(result)
    }

    @Test
    fun qr2() {
        var failed = 0

        val pdfFolder = File(javaClass.getResource("/qr_pdf").path)
        pdfFolder.listFiles().forEach { file ->
            val pdfStream = file.inputStream()
            val renderer = PdfRenderer(pdfStream)

            val qrReader = QrReader()

            renderer.forEach { p ->
                val result = qrReader.read(p)
                result ?: failed++
            }

            pdfStream.close()
        }

        println(failed)
    }
}