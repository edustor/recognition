package ru.edustor.upload.pages

import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

class WebpIO() {
    val TEMP_PREFIX = "edustor-upload-covert"

    fun encode(image: RenderedImage): ByteArray {
        val tempPngFile = File.createTempFile(TEMP_PREFIX, ".png")
        tempPngFile.deleteOnExit()
        ImageIO.write(image, "png", tempPngFile)

        val tempWebpFile = File.createTempFile(TEMP_PREFIX, ".webp")
        tempWebpFile.deleteOnExit()

        val process = ProcessBuilder("cwebp", "-q", "50", tempPngFile.absolutePath, "-o", tempWebpFile.absolutePath).start()
        process.waitFor()

        val webpBytes = tempWebpFile.readBytes()

        tempPngFile.delete()
        tempWebpFile.delete()

        return webpBytes
    }

    fun decode(imageStream: InputStream): BufferedImage {
        val tempWebpFile = File.createTempFile(TEMP_PREFIX, ".webp")
        val tempPngFile = File.createTempFile(TEMP_PREFIX, ".png")

        tempPngFile.deleteOnExit()
        tempWebpFile.deleteOnExit()

        tempWebpFile.outputStream().use {
            imageStream.copyTo(it)
        }

        val process = ProcessBuilder("dwebp", tempWebpFile.absolutePath, "-o", tempPngFile.absolutePath).start()
        process.waitFor()

        val image = ImageIO.read(tempPngFile)

        tempPngFile.delete()
        tempWebpFile.delete()

        return image
    }
}