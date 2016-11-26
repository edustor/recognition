package ru.edustor.upload.pdf

import org.junit.jupiter.api.Test

class TestPdfRenderer {
    @Test
    internal fun checkPageRenders() {
        val blankPdfPageStream = javaClass.getResource("/blank1.pdf").openStream()
        val renderer = PdfRenderer(blankPdfPageStream)
        renderer.next()
    }
}