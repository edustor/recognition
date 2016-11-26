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

}