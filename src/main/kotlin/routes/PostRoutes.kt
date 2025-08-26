package com.codewithngoc.instagallery.routes

import com.codewithngoc.instagallery.domain.models.AddCommentRequest
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

                // ✅ Thêm route bình luận bài đăng (POST)
                // ✅ Route thêm bình luận cho bài viết
                post("{postId}/comments") {
                    val principal = call.principal<AuthPrincipal>()
                    if (principal == null) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("message" to "Bạn chưa đăng nhập")
                        )
                        return@post
                    }

                    val postId = call.parameters["postId"]?.toIntOrNull()
                    if (postId == null || postId <= 0) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("message" to "ID bài viết không hợp lệ")
                        )
                        return@post
                    }

                    val request = runCatching { call.receive<AddCommentRequest>() }.getOrNull()
                    if (request == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("message" to "Dữ liệu request không hợp lệ")
                        )
                        return@post
                    }

                    // ✅ Kiểm tra nội dung bình luận
                    if (request.content.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("message" to "Nội dung bình luận không được để trống")
                        )
                        return@post
                    }
                    if (request.content.length > 500) { // Giới hạn độ dài
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("message" to "Nội dung bình luận quá dài, tối đa 500 ký tự")
                        )
                        return@post
                    }

                    // Thêm bình luận
                    postService.addComment(postId, principal.userId, request).fold(
                        onSuccess = { commentResponse ->
                            call.respond(HttpStatusCode.Created, commentResponse)
                        },
                        onFailure = { e ->
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("message" to "Lỗi khi thêm bình luận: ${e.localizedMessage}")
                            )
                        }
                    )
                }


                // ✅ Route lấy danh sách bình luận (GET)
                get("{postId}/comments") {
                    try {
                        val principal = call.principal<AuthPrincipal>()
                        if (principal == null) {
                            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Không được ủy quyền"))
                            return@get
                        }

                        val postId = call.parameters["postId"]?.toIntOrNull()
                        if (postId == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("message" to "ID bài đăng không hợp lệ"))
                            return@get
                        }

                        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

                        if (page < 0 || size <= 0) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Tham số page hoặc size không hợp lệ"))
                            return@get
                        }

                        postService.getCommentsForPost(postId, page, size).fold(
                            onSuccess = { comments ->
                                if (comments.isEmpty()) {
                                    call.respond(HttpStatusCode.OK, mapOf("message" to "Chưa có bình luận nào"))
                                } else {
                                    val sortedComments = comments.sortedByDescending { it.createdAt }
                                    call.respond(HttpStatusCode.OK, comments)
                                }
                            },
                            onFailure = { e ->
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    mapOf("message" to "Lỗi khi lấy danh sách bình luận: ${e.localizedMessage}")
                                )
                            }
                        )
                    } catch (e: Exception) {
                        // Bắt mọi lỗi không mong muốn để tránh 500 vô lý
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("message" to "Đã xảy ra lỗi: ${e.localizedMessage}")
                        )
                    }
                }

                // ✅ THÊM ROUTE LẤY BÀI ĐĂNG CỦA NGƯỜI DÙNG TẠI ĐÂY
                get("user/{userId}") {
                    val principal = call.principal<AuthPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Không được ủy quyền"))
                        return@get
                    }

                    val userId = call.parameters["userId"]?.toIntOrNull()
                    if (userId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("message" to "ID người dùng không hợp lệ"))
                        return@get
                    }

                    // 🔴 Bắt lỗi trực tiếp bằng try-catch
                    try {
                        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

                        val posts = postService.getUserPosts(userId, page, size)

                        // 🔴 Sử dụng .fold để xử lý Result
                        posts.fold(
                            onSuccess = { postResponses ->
                                if (postResponses.isEmpty()) {
                                    // Xử lý trường hợp không có bài viết nào
                                    call.respond(HttpStatusCode.OK, mapOf("message" to "Người dùng này chưa có bài đăng nào"))
                                } else {
                                    call.respond(HttpStatusCode.OK, postResponses)
                                }
                            },
                            onFailure = { e ->
                                // 🔴 Kiểm tra và trả về status code cụ thể
                                when (e.message) {
                                    "User not found" -> call.respond(HttpStatusCode.NotFound, mapOf("message" to "Người dùng không tồn tại"))
                                    "Unauthorized" -> call.respond(HttpStatusCode.Forbidden, mapOf("message" to "Bạn không có quyền xem bài viết của người dùng này"))
                                    else -> {
                                        // Trả về lỗi server chung nếu không khớp
                                        call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Lỗi khi lấy bài đăng: ${e.message}"))
                                    }
                                }
                            }
                        )
                    } catch (e: Exception) {
                        // 🔴 Bắt mọi ngoại lệ không mong muốn để tránh crash
                        call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Đã xảy ra lỗi không xác định: ${e.message}"))
                    }
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
