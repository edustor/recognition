package ru.edustor.upload.pages

import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class TestPdfRenderer {
    var image: BufferedImage? = null

    val ARTIFACTS_DIR = File("build/test-results")

    init {
        ARTIFACTS_DIR.mkdirs()
    }

    @Test
    fun pageRenders() {
        val blankPdfPageStream = javaClass.getResource("/scanned.pdf").openStream()
        val renderer = PdfRenderer(blankPdfPageStream)
        image = renderer.next()
        val outFile = File(ARTIFACTS_DIR, "pdf-rendered.png")
        outFile.parentFile.mkdirs()
        ImageIO.write(image, "png", outFile)
    }

    @Test
    fun saveWebp() {
        val webpIO = WebpIO()

        val sourceImageURL = javaClass.getResource("/webp_src_image.png")
        val sourceImage = ImageIO.read(sourceImageURL)
        val bytes = webpIO.encode(sourceImage)

        val file = File(ARTIFACTS_DIR, "webp_image.webp")
        file.parentFile.mkdirs()

        file.writeBytes(bytes)
    }

    @Test
    fun loadWebp() {
        val webpIO = WebpIO()

        var image: BufferedImage? = null

        javaClass.getResource("/webp_image.webp").openStream().use { sourceWebpStream ->
            image = webpIO.decode(sourceWebpStream)
        }

        val file = File(ARTIFACTS_DIR, "decoded_webp_image.png")
        file.parentFile.mkdirs()

        ImageIO.write(image, "png", file)
    }
}