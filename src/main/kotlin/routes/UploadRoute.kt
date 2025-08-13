package com.codewithngoc.instagallery.routes

import com.codewithngoc.instagallery.domain.services.FileService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.uploadRoutes(fileService: FileService) {
    post("/upload") {
        val multipart = call.receiveMultipart()
        var fileUrl: String? = null

        multipart.forEachPart { part ->
            if (part is PartData.FileItem) {
                val fileName = part.originalFileName ?: "uploaded_file"
                val fileStream = part.streamProvider()

                fileUrl = fileService.saveFile(fileStream, fileName)
                part.dispose()
            }
        }

        if (fileUrl != null) {
            call.respond(HttpStatusCode.OK, mapOf("url" to fileUrl))
        } else {
            call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Không tìm thấy file trong request"))
        }
    }
}