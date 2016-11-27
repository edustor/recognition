package ru.edustor.recognition.pages

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO

class TestQR {

    val ARTIFACTS_DIR = File("build/test-results/qr")

    init {
        ARTIFACTS_DIR.mkdirs()
    }

    @Test
    fun qr() {
        val srcFolder = File(javaClass.getResource("/qr_test").path)
        val qrReader = QrReader()

        srcFolder.listFiles().forEach { file ->
            val image = ImageIO.read(file)
            val result = qrReader.read(image)
            Assertions.assertNotNull(result)
        }
    }

    @Test
    fun testReadSubimage() {
        val srcFolder = File(javaClass.getResource("/qr_test").path)
        val qrReader = QrReader()

        srcFolder.listFiles().forEach { file ->
            val image = ImageIO.read(file)

            val (qrResult, subimage) = qrReader.readSubimage(image)

            val croppedFile = File(ARTIFACTS_DIR, "${file.nameWithoutExtension}.cropped.png")
            ImageIO.write(subimage, "png", croppedFile)
            Assertions.assertNotNull(qrResult)
        }
    }

    @Test
    fun blankQrReadFails() {
        val image = ImageIO.read(javaClass.getResource("/blank.png"))
        val qrReader = QrReader()
        val result = qrReader.read(image)
        Assertions.assertNull(result)
    }
}