package ru.edustor.upload.pages

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.NotFoundException
import com.google.zxing.ReaderException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class QrReader {

    val logger = LoggerFactory.getLogger(QrReader::class.java)

    fun read(image: BufferedImage): String? {
        var result = zxingRead(image)

        result ?: let {
            logger.warn("ZXing failed to read qr code. Falling back to zbar")
            result = zbarRead(image)
            result ?: logger.warn("Both scanners failed to read qr code")
        }

        logger.debug("Read: $result")

        return result
    }

    fun zxingRead(image: BufferedImage): String? {
        val reader = QRCodeReader()
        val luminanceSource = BufferedImageLuminanceSource(image)
        val binarizer = HybridBinarizer(luminanceSource)
        val binaryBitmap = BinaryBitmap(binarizer)

        try {
            val result = reader.decode(binaryBitmap, mapOf(
                    DecodeHintType.TRY_HARDER to true
            ))
            return result.text
        } catch (e: ReaderException) {
            return null
        }
    }

    fun zbarRead(image: BufferedImage): String? {
        val tempFile = File.createTempFile("edustor-qr", ".tmp.png")
        ImageIO.write(image, "png", tempFile)

        val process = Runtime.getRuntime().exec(arrayOf(
                "zbarimg", "-q", "--raw", tempFile.absolutePath
        ))

        val result = process.inputStream.reader().readLines()

        tempFile.delete()

        return result.getOrNull(0)
    }
}
