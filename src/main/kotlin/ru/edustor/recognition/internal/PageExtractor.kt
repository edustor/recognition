package ru.edustor.recognition.internal

import org.ghost4j.document.PDFDocument
import java.io.InputStream

class PageExtractor(pdfStream: InputStream) : Iterator<ByteArray> {

    var nextPage = 1
    val lastPage: Int
    val pdfDocument = PDFDocument()

    init {
        pdfDocument.load(pdfStream)

        lastPage = pdfDocument.pageCount
    }

    override fun hasNext(): Boolean {
        return nextPage <= lastPage
    }

    override fun next(): ByteArray {
        return pdfDocument.extract(nextPage, nextPage++).content
    }
}