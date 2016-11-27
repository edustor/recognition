package ru.edustor.recognition.internal

import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class TestPdfRenderer {
    var image: BufferedImage? = null

    val ARTIFACTS_DIR = File("build/test-results/pdf")

    init {
        ARTIFACTS_DIR.mkdirs()
    }

    @Test
    fun pageRenders() {
        val blankPdfPageStream = javaClass.getResource("/scanned.pdf").openStream()
        val renderer = PdfRenderer(blankPdfPageStream, 150)
        image = renderer.next()
        val outFile = File(ARTIFACTS_DIR, "pdf-rendered.png")
        outFile.parentFile.mkdirs()
        ImageIO.write(image, "png", outFile)
    }

}