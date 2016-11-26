package ru.edustor.upload.pages

import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class TestPdfRenderer {
    var image: BufferedImage? = null

    @Test
    fun pageRenders() {
        val blankPdfPageStream = javaClass.getResource("/scanned.pdf").openStream()
        val renderer = PdfRenderer(blankPdfPageStream)
        image = renderer.next()
        val outFile = File("build/test-results/pdf-rendered.png")
        outFile.parentFile.mkdirs()
        ImageIO.write(image, "png", outFile)
    }

    @Test
    fun saveWebp() {
        val sourceImageURL = javaClass.getResource("/webp_src_image.png")
        val sourceImage = ImageIO.read(sourceImageURL)
        val webpImage = WebpImage(sourceImage)
        val bytes = webpImage.encode()

        val file = File("build/test-results/webp_image.webp")
        file.createNewFile()

        file.writeBytes(bytes)
    }
}