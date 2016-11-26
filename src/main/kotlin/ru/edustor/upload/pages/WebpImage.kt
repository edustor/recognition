package ru.edustor.upload.pages

import java.awt.image.RenderedImage
import java.io.File
import javax.imageio.ImageIO

class WebpImage(val image: RenderedImage) {
    fun encode(): ByteArray {
        val tempPngFile = File.createTempFile("edustor-upload-covert", ".png")
        tempPngFile.deleteOnExit()
        ImageIO.write(image, "png", tempPngFile)

        val tempWebpFile = File.createTempFile("edustor-upload-covert", ".webp")
        tempWebpFile.deleteOnExit()

        val process = ProcessBuilder("cwebp", "-q", "50", tempPngFile.absolutePath, "-o", tempWebpFile.absolutePath).start()
        process.waitFor()

        val webpBytes = tempWebpFile.readBytes()

        tempPngFile.delete()
        tempWebpFile.delete()

        return webpBytes
    }
}