package ru.edustor.recognition.service

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.edustor.recognition.exception.PdfNotFoundException
import java.io.InputStream

@Service
open class PdfStorageService(
        @Value("\${S3_URL}") url: String,
        @Value("\${S3_ACCESS_KEY}") accessKey: String,
        @Value("\${S3_SECRET_KEY}") secretKey: String
) {
    private val minio: MinioClient

    init {
        minio = MinioClient(url, accessKey, secretKey)
    }

    open fun getUploadedPdf(uuid: String): InputStream {
        return minio.getObject("edustor-pdf-uploads", "$uuid.pdf")
                ?: throw PdfNotFoundException("Failed to find $uuid.pdf in edustor-pdf-uploads bucket")
    }

    open fun putPagePdf(pagePdfUuid: String, inputStream: InputStream, size: Long) {
        minio.putObject("edustor-pdf-recognised", "$pagePdfUuid.pdf", inputStream, size, "application/pdf")
    }
}