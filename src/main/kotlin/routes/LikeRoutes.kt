package com.codewithngoc.instagallery.routes

import com.codewithngoc.instagallery.domain.models.AuthPrincipal
import com.codewithngoc.instagallery.domain.services.LikeService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.likeRoutes(likeService: LikeService) {
    routing {
        authenticate("auth-jwt") {
            route("api/posts/{postId}/likes") {

                // ✅ Lấy danh sách người đã like bài viết
                get {
                    val postId = call.parameters["postId"]?.toIntOrNull()
                    if (postId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("message" to "❌ ID bài viết không hợp lệ"))
                        return@get
                    }

                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1 // Mặc định trang 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20 // Mặc định 20 mục mỗi trang

                    likeService.getLikesForPost(postId, page, size).fold(
                        onSuccess = { likes ->
                            call.respond(HttpStatusCode.OK, likes)
                        },
                        onFailure = { e ->
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("message" to "⚠️ Lỗi khi lấy danh sách like: ${e.message}")
                            )
                        }
                    )
                }

                // ✅ Người dùng like bài viết
                post {
                    val principal = call.principal<AuthPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "❌ Không được ủy quyền"))

                    val postId = call.parameters["postId"]?.toIntOrNull()
                    if (postId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("message" to "❌ ID bài viết không hợp lệ"))
                        return@post
                    }

                    likeService.likePost(postId, principal).fold(
                        onSuccess = {
                            call.respond(HttpStatusCode.Created, mapOf("message" to "❤️ Đã like bài viết"))
                        },
                        onFailure = { e ->
                            when (e.message) {
                                "Already liked" -> call.respond(HttpStatusCode.Conflict, mapOf("message" to "⚠️ Bạn đã like bài viết này rồi"))
                                else -> call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "⚠️ Lỗi khi like: ${e.message}"))
                            }
                        }
                    )
                }

                // ✅ Người dùng bỏ like bài viết
                delete {
                    val principal = call.principal<AuthPrincipal>()
                        ?: return@delete call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "❌ Không được ủy quyền"))

                    val postId = call.parameters["postId"]?.toIntOrNull()
                    if (postId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("message" to "❌ ID bài viết không hợp lệ"))
                        return@delete
                    }

                    likeService.unlikePost(postId, principal).fold(
                        onSuccess = {
                            call.respond(HttpStatusCode.OK, mapOf("message" to "💔 Đã bỏ like bài viết"))
                        },
                        onFailure = { e ->
                            when (e.message) {
                                "Not liked yet" -> call.respond(HttpStatusCode.NotFound, mapOf("message" to "❌ Bạn chưa like bài viết này"))
                                else -> call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "⚠️ Lỗi khi bỏ like: ${e.message}"))
                            }
                        }
                    )
                }

                // ✅ Kiểm tra người dùng đã like chưa
                get("check") {
                    val principal = call.principal<AuthPrincipal>()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "❌ Không được ủy quyền"))

                    val postId = call.parameters["postId"]?.toIntOrNull()
                    if (postId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("message" to "❌ ID bài viết không hợp lệ"))
                        return@get
                    }

                    likeService.hasUserLiked(postId, principal).fold(
                        onSuccess = { hasLiked ->
                            call.respond(HttpStatusCode.OK, mapOf("liked" to hasLiked))
                        },
                        onFailure = { e ->
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("message" to "⚠️ Lỗi khi kiểm tra like: ${e.message}")
                            )
                        }
                    )
                }
            }
        }
    }
}
