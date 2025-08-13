package com.codewithngoc.instagallery.routes

import com.codewithngoc.instagallery.domain.models.AuthPrincipal
import com.codewithngoc.instagallery.domain.models.CreatePostRequest
import com.codewithngoc.instagallery.domain.models.UpdatePostRequest
import com.codewithngoc.instagallery.domain.services.PostService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.postRoutes(postService: PostService) {
    routing {
        // Các route cần xác thực (JWT)
        authenticate("auth-jwt") {

            // ✅ THÊM ROUTE CHO ADMIN Ở ĐÂY
            route("api/admin") {

                // ✅ Thêm route xoá toàn bộ
                delete("delAllPost") {
                    val principal = call.principal<AuthPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Không được ủy quyền"))
                        return@delete
                    }

                    postService.deleteAllPosts(principal).fold(
                        onSuccess = {
                            call.respond(HttpStatusCode.OK, mapOf("message" to "🧹 Đã xoá toàn bộ bài đăng thành công"))
                        },
                        onFailure = { e ->
                            when (e.message) {
                                "Unauthorized" -> call.respond(HttpStatusCode.Forbidden, mapOf("message" to "Chỉ admin mới được xoá toàn bộ bài viết"))
                                else -> call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Lỗi khi xoá tất cả bài viết: ${e.message}"))
                            }
                        }
                    )
                }
            }

            route("api/posts") {
                // Lấy tất cả bài đăng từ database (Chỉ admin)
                get {
                    // Lấy thông tin principal nếu cần
                    val principal = call.principal<AuthPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Bạn cần đăng nhập"))
                        return@get
                    }

                    postService.getAllPosts().fold(
                        onSuccess = { posts ->
                            call.respond(HttpStatusCode.OK, posts)
                        },
                        onFailure = { e ->
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("message" to "Lỗi khi lấy tất cả bài đăng: ${e.message}")
                            )
                        }
                    )
                }

                // Tạo bài đăng
                post {
                    val principal = call.principal<AuthPrincipal>() ?: return@post call.respond(
                        HttpStatusCode.Unauthorized, error("Không được ủy quyền")
                    )

                    val request = runCatching { call.receive<CreatePostRequest>() }
                        .onFailure { e ->
                            e.printStackTrace()
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                error("Dữ liệu request không hợp lệ: ${e.localizedMessage}")
                            )
                        }.getOrNull() ?: return@post

                    postService.createPost(principal.userId, request).fold(
                        onSuccess = { post -> call.respond(HttpStatusCode.Created, post) },
                        onFailure = { e ->
                            e.printStackTrace()
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                error("Lỗi khi tạo bài đăng: ${e.localizedMessage ?: "Không xác định"}")
                            )
                        }
                    )
                }


                // Cập nhật bài viết
                put("{postId}") {
                    val principal = call.principal<AuthPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Không được ủy quyền"))
                        return@put
                    }

                    val postId = call.parameters["postId"]?.toIntOrNull()
                    if (postId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("message" to "ID bài đăng không hợp lệ"))
                        return@put
                    }

                    val request = try {
                        call.receive<UpdatePostRequest>()
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Dữ liệu request không hợp lệ: ${e.message}"))
                        return@put
                    }

                    postService.updatePost(postId, principal, request).fold(
                        onSuccess = { postResponse -> call.respond(HttpStatusCode.OK, postResponse) },
                        onFailure = { e ->
                            when (e.message) {
                                "Post not found" -> call.respond(HttpStatusCode.NotFound, mapOf("message" to "Bài đăng không tìm thấy"))
                                "Unauthorized" -> call.respond(HttpStatusCode.Forbidden, mapOf("message" to "Bạn không có quyền cập nhật bài đăng này"))
                                else -> call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Lỗi khi cập nhật bài đăng: ${e.message}"))
                            }
                        }
                    )
                }

                // Xoá bài viết
                delete("{postId}") {
                    val principal = call.principal<AuthPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Không được ủy quyền"))
                        return@delete
                    }

                    val postId = call.parameters["postId"]?.toIntOrNull()
                    if (postId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("message" to "ID bài đăng không hợp lệ"))
                        return@delete
                    }

                    postService.deletePost(postId, principal).fold(
                        onSuccess = { success ->
                            if (success) {
                                call.respond(HttpStatusCode.OK, mapOf("message" to "✅ Đã xoá bài đăng thành công"))
                            } else {
                                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Bài đăng không tìm thấy hoặc không thể xóa"))
                            }
                        },
                        onFailure = { e ->
                            when (e.message) {
                                "Post not found" -> call.respond(HttpStatusCode.NotFound, mapOf("message" to "Bài đăng không tìm thấy"))
                                "Unauthorized" -> call.respond(HttpStatusCode.Forbidden, mapOf("message" to "Không có quyền xóa bài đăng này"))
                                else -> call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Lỗi khi xóa bài đăng: ${e.message}"))
                            }
                        }
                    )
                }

                // Lấy danh sách feed
                get("/feed") {
                    val principal = call.principal<AuthPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Không được ủy quyền"))
                        return@get
                    }

                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

                    postService.getFeedPosts(principal.userId, page, size).fold(
                        onSuccess = { posts -> call.respond(HttpStatusCode.OK, posts) },
                        onFailure = { e -> call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Lỗi khi lấy bài đăng feed: ${e.message}")) }
                    )
                }
            }
        }


        // Các route không cần xác thực
        route("api/posts") {

            // Lấy bài viết theo ID
            get("{postId}") {
                val postId = call.parameters["postId"]?.toIntOrNull()
                if (postId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "ID bài đăng không hợp lệ"))
                    return@get
                }

                val principal = call.principal<AuthPrincipal>() // Optional

                postService.getPostById(postId, principal).fold(
                    onSuccess = { postResponse -> call.respond(HttpStatusCode.OK, postResponse) },
                    onFailure = { e ->
                        when (e.message) {
                            "Post not found" -> call.respond(HttpStatusCode.NotFound, mapOf("message" to "Bài đăng không tìm thấy"))
                            "Access denied" -> call.respond(HttpStatusCode.Forbidden, mapOf("message" to "Bạn không có quyền xem bài đăng này"))
                            else -> call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Lỗi khi lấy bài đăng: ${e.message}"))
                        }
                    }
                )
            }

            // Lấy danh sách bài viết khám phá
            get("/explore") {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

                postService.getExplorePosts(page, size).fold(
                    onSuccess = { posts -> call.respond(HttpStatusCode.OK, posts) },
                    onFailure = { e -> call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Lỗi khi lấy bài đăng khám phá: ${e.message}")) }
                )
            }
        }
    }
}
