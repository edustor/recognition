package ru.edustor.upload.pdf

import org.ghost4j.document.PDFDocument
import org.ghost4j.renderer.SimpleRenderer
import java.awt.Image
import java.io.InputStream
import java.util.*

class PdfRenderer(pdfStream: InputStream) : Iterator<Image> {
    val renderer: SimpleRenderer
    val pdfDocument: PDFDocument

    var nextPage = 0
    val lastPage: Int

    init {
        pdfDocument = PDFDocument()
        pdfDocument.load(pdfStream)
        lastPage = pdfDocument.pageCount - 1

        renderer = SimpleRenderer()
        renderer.resolution = 300
    }

    override fun hasNext(): Boolean {
        return nextPage <= lastPage
    }

    override fun next(): Image {
        if (!hasNext()) throw NoSuchElementException()
        return renderer.render(pdfDocument, nextPage++, nextPage)[0]
    }
}