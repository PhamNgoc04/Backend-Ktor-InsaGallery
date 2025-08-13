package com.codewithngoc.instagallery.domain.services

import java.io.File
import java.io.InputStream
import java.util.*

class FileService {
    private val uploadDir = File("media").apply { mkdirs() }
    private val serverBaseUrl = "http://10.0.2.2:8080" // Dùng IP nội bộ nếu test trên thiết bị thật

    fun saveFile(inputStream: InputStream, originalFileName: String): String {
        val uniqueFileName = "${UUID.randomUUID()}-${originalFileName}"
        val file = File(uploadDir, uniqueFileName)

        file.outputStream().use { fos ->
            inputStream.copyTo(fos)
        }

        return "$serverBaseUrl/media/$uniqueFileName"
    }
}