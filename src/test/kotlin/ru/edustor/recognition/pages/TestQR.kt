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
        val image = ImageIO.read(javaClass.getResource("/zxing_test.png"))
        val qrReader = QrReader()

        val result = qrReader.read(image)

        Assertions.assertNotNull(result)
    }

    @Test
    fun testReadSubimage() {
        val srcFolder = File(javaClass.getResource("/qr_test").path)

        srcFolder.listFiles().forEach { file ->
            val image = ImageIO.read(file)

            val QR_REGION_SIZE_PERCENT = 0.125
            val QR_margin_SIZE_PERCENT = 0.01

            val qrReginSize = (image.width * QR_REGION_SIZE_PERCENT).toInt()

            val xMargin = (image.width * QR_margin_SIZE_PERCENT).toInt()
            val yMargin = (image.height * QR_margin_SIZE_PERCENT).toInt()

            val subimage = image.getSubimage(
                    image.width - (qrReginSize + xMargin),
                    image.height - (qrReginSize + yMargin),
                    qrReginSize,
                    qrReginSize
            )

            val croppedFile = File(ARTIFACTS_DIR, "${file.nameWithoutExtension}.cropped.png")
            ImageIO.write(subimage, "png", croppedFile)

            val qrReader = QrReader()
            val result = qrReader.read(subimage)
        }
    }
}