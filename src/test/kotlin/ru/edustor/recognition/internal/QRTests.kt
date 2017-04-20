package ru.edustor.recognition.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

class QRTests {

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
            assertThat(result).isNotNull()
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
            assertThat(qrResult).isNotNull()
        }
    }

    @Test
    fun blankQrReadFails() {
        val image = ImageIO.read(javaClass.getResource("/blank.png"))
        val qrReader = QrReader()
        val result = qrReader.read(image)
        assertThat(result).isNull()
    }
}