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

    val uploads_bucket = "edustor-pdf-uploads"
    val processed_pages_bucket = "edustor-pages-alpha"

    init {
        minio = MinioClient(url, accessKey, secretKey)
        if (!minio.bucketExists(processed_pages_bucket)) {
            minio.makeBucket(processed_pages_bucket)
        }
    }

    open fun getUploadedPdf(uuid: String): InputStream {
        return minio.getObject(uploads_bucket, "$uuid.pdf")
                ?: throw PdfNotFoundException("Failed to find $uuid.pdf in edustor-pdf-uploads bucket")
    }

    open fun deleteUploadedPdf(uuid: String) {
        minio.removeObject(uploads_bucket, "$uuid.pdf")
    }

    open fun putPagePdf(pagePdfUuid: String, inputStream: InputStream, size: Long) {
        minio.putObject(processed_pages_bucket, "$pagePdfUuid.pdf", inputStream, size, "application/pdf")
    }
}