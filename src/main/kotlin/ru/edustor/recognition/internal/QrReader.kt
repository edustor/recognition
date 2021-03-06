package ru.edustor.recognition.internal

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.ReaderException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class QrReader {

    val logger: Logger = LoggerFactory.getLogger(QrReader::class.java)

    fun read(image: BufferedImage, trySubimage: Boolean = true): String? {
        var result = zxingRead(image)
        if (result != null) {
            logger.debug("ZXing read: $result")
            return result
        }
        logger.warn("ZXing failed to read qr code. Falling back to zbar")

        result = zbarRead(image)
        if (result != null) {
            logger.debug("ZBar read: $result")
            return result
        }
        logger.warn("Both scanners failed to read qr code")

        if (trySubimage) {
            logger.info("Trying to scan subimage")
            result = readSubimage(image).first

            if (result != null) {
                return result
            }

            logger.warn("Subimage read failed")
        }

        return null
    }

    fun readSubimage(image: BufferedImage): Pair<String?, BufferedImage> {
        val QR_REGION_SIZE_PERCENT = 0.125
        val QR_MARGIN_SIZE_PERCENT = 0.01

        val qrReginSize = (image.width * QR_REGION_SIZE_PERCENT).toInt()

        val xMargin = (image.width * QR_MARGIN_SIZE_PERCENT).toInt()
        val yMargin = (image.height * QR_MARGIN_SIZE_PERCENT).toInt()

        val subimage = image.getSubimage(
                image.width - (qrReginSize + xMargin),
                image.height - (qrReginSize + yMargin),
                qrReginSize,
                qrReginSize
        )

        val result = read(subimage, trySubimage = false)

        return result to subimage
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
