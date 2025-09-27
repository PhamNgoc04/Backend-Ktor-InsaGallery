package com.codewithngoc.instagallery.routes

import com.codewithngoc.instagallery.db.tables.Role
import com.codewithngoc.instagallery.domain.models.AuthPrincipal // Đảm bảo AuthPrincipal của bạn được định nghĩa đúng
import com.codewithngoc.instagallery.domain.models.LoginRequest
import com.codewithngoc.instagallery.domain.models.RegisterRequest
import com.codewithngoc.instagallery.domain.models.UpdateProfileRequest
import com.codewithngoc.instagallery.domain.services.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put


fun Application.authRoutes(authService: AuthService) {
    routing {

        authenticate("auth-jwt") {
            route("/api/admin") {

                /**
                 * 📌 Tạo tài khoản Admin mới
                 * Endpoint: POST /api/admin/create
                 * Yêu cầu: SUPER_ADMIN mới có quyền
                 */
                post("/create") {
                    val request = runCatching { call.receive<RegisterRequest>() }
                        .onFailure {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "❌ Dữ liệu request không hợp lệ")
                            )
                            return@post
                        }.getOrThrow()

                    // ✅ Kiểm tra quyền SUPER_ADMIN
                    val principal = call.principal<AuthPrincipal>()
                    if (principal == null || principal.role != Role.ADMIN.name) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "⛔ Bạn không có quyền tạo admin")
                        )
                        return@post
                    }

                    // ✅ Kiểm tra dữ liệu đầu vào
                    when {
                        request.username.isNullOrBlank() -> {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Tên không được để trống"))
                            return@post
                        }
                        request.fullName.isNullOrBlank() -> {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Tên đầy đủ không được để trống"))
                            return@post
                        }
                        request.email.isNullOrBlank() -> {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email không được để trống"))
                            return@post
                        }
                        request.password.isNullOrBlank() -> {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Mật khẩu không được để trống"))
                            return@post
                        }
                    }

                    // ✅ Gọi service để tạo Admin
                    authService.registerAdmin(request).fold(
                        onSuccess = { authResponse ->
                            call.respond(HttpStatusCode.Created, authResponse)
                        },
                        onFailure = { error ->
                            call.respond(
                                HttpStatusCode.Conflict,
                                mapOf("error" to (error.message ?: "❌ Tạo admin thất bại"))
                            )
                        }
                    )
                }
            }
        }

        route("/api/auth") {
            /**
             * 📌 Đăng ký tài khoản mới
             * Endpoint: POST /api/auth/register
             */
            post("/register") {
                val request = runCatching { call.receive<RegisterRequest>() }
                    .onFailure {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "❌ Dữ liệu request không hợp lệ"))
                        return@post
                    }.getOrThrow()

                // Kiểm tra từng field
                when {
                    request.username.isNullOrBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Tên không được để trống"))
                        return@post
                    }
                    request.fullName.isNullOrBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Tên đầy đủ không được để trống"))
                        return@post
                    }
                    request.email.isNullOrBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email không được để trống"))
                        return@post
                    }
                    request.password.isNullOrBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Mật khẩu không được để trống"))
                        return@post
                    }

                }

                authService.registerUser(request).fold(
                    onSuccess = { authResponse ->
                        call.respond(HttpStatusCode.Created, authResponse)
                    },
                    onFailure = { error ->
                        // Tránh tiết lộ chi tiết lỗi nội bộ
                        call.respond(HttpStatusCode.Conflict, mapOf("error" to (error.message ?: "Registration failed")))
                    }
                )
            }

            /**
             * 📌 Đăng nhập
             * Endpoint: POST /api/auth/login
             */
            post("/login") {
                val request = runCatching { call.receive<LoginRequest>() }
                    .onFailure {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "❌ Dữ liệu request không hợp lệ"))
                        return@post
                    }.getOrThrow()

                // Kiểm tra từng field
                when {
                    request.email.isNullOrBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email không được để trống"))
                        return@post
                    }
                    request.password.isNullOrBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Mật khẩu không được để trống"))
                        return@post
                    }
                }

                authService.loginUser(request).fold(
                    onSuccess = { loginResponse ->
                        call.respond(HttpStatusCode.OK, loginResponse)
                    },
                    onFailure = {
                        // Dùng thông báo chung để tránh tiết lộ logic xác thực
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                    }
                )
            }




            // --- Các route được bảo vệ bằng JWT ---
            authenticate("auth-jwt") {

                // ✅ MỚI: Endpoint đăng xuất
                /**
                 * 📌 Đăng xuất
                 * Yêu cầu refresh token trong body để vô hiệu hóa session.
                 */
                post("/logout") {
                    val refreshToken = try {
                        call.receive<Map<String, String>>()["refreshToken"]?.trim()
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "❌ Missing refreshToken in request body"))
                        return@post
                    }

                    if (refreshToken.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "❌ refreshToken is required"))
                        return@post
                    }

                    val result = authService.logout(refreshToken)
                    result.fold(
                        onSuccess = { success ->
                            if (success) {
                                call.respond(HttpStatusCode.OK, mapOf("message" to "✅ Logged out successfully"))
                            } else {
                                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Refresh token not found or already invalidated"))
                            }
                        },
                        onFailure = { error ->
                            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (error.message ?: "Failed to logout")))
                        }
                    )
                }

                /**
                 * 📌 Lấy thông tin user theo ID
                 * User chỉ có thể lấy thông tin của chính mình.
                 * Admin có thể lấy thông tin của bất kỳ user nào.
                 */
                get("/profile/{id}") {
                    val requestedId = call.parameters["id"]?.toIntOrNull()
                    if (requestedId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "❌ Invalid user ID format"))
                        return@get
                    }

                    // Lấy principal đã được xác thực
                    val principal = call.principal<AuthPrincipal>()

                    // Kiểm tra nếu principal không tồn tại (lỗi xác thực JWT)
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "⛔ Authentication required"))
                        return@get
                    }

                    // Logic ủy quyền:
                    // 1. Nếu là ADMIN, được phép truy cập bất kỳ ID nào.
                    // 2. Nếu không phải ADMIN, chỉ được phép truy cập ID của chính mình.
                    val isAuthorized = principal.role == "ADMIN" || principal.userId == requestedId

                    if (!isAuthorized) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "⛔ Permission denied"))
                        return@get
                    }

                    val result = authService.getUserById(requestedId)
                    result.fold(
                        onSuccess = { call.respond(HttpStatusCode.OK, it) },
                        onFailure = {
                            // Cung cấp thông báo lỗi chung hơn nếu không tìm thấy người dùng
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                        }
                    )
                }


                /**
                 * 📌 Cập nhật thông tin người dùng
                 * User chỉ có thể cập nhật thông tin của chính mình.
                 * Admin có thể cập nhật thông tin của bất kỳ user nào.
                 */
                put("/profile/{id}") {
                    val requestedId = call.parameters["id"]?.toIntOrNull()
                    if (requestedId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "❌ Invalid user ID format"))
                        return@put
                    }

                    val principal = call.principal<AuthPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "⛔ Authentication required"))
                        return@put
                    }

                    // Logic ủy quyền:
                    // 1. Nếu là ADMIN, được phép cập nhật bất kỳ ID nào.
                    // 2. Nếu không phải ADMIN, chỉ được phép cập nhật ID của chính mình.
                    val isAuthorized = principal.role == "ADMIN" || principal.userId == requestedId

                    if (!isAuthorized) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "⛔ Permission denied"))
                        return@put
                    }

                    val updateRequest = try {
                        call.receive<UpdateProfileRequest>()
                    } catch (e: Exception) {
                        // Tránh tiết lộ chi tiết lỗi nội bộ về cấu trúc request
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "❌ Invalid request body format or missing fields"))
                        return@put
                    }

                    val result = authService.updateUserProfile(requestedId, updateRequest)
                    result.fold(
                        onSuccess = { call.respond(HttpStatusCode.OK, it) },
                        onFailure = { error ->
                            // Cung cấp thông báo lỗi chung hơn
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Failed to update user profile"))
                        }
                    )
                }

                /**
                 * 🗑️ Xóa người dùng theo ID
                 * Chỉ ADMIN mới có quyền xóa người dùng.
                 * (Nếu muốn cho phép tự xóa, cần thêm logic kiểm tra principal.userId == requestedId)
                 */
                delete("/profile/{id}") {
                    val requestedId = call.parameters["id"]?.toIntOrNull()
                    if (requestedId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "❌ Invalid user ID format")
                        )
                        return@delete
                    }

                    val principal = call.principal<AuthPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "⛔ Authentication required"))
                        return@delete
                    }

                    // Chỉ ADMIN mới được phép xóa người dùng khác
                    // Nếu muốn cho phép người dùng tự xóa tài khoản của mình:
                    // val isAuthorized = principal.role == "ADMIN" || principal.userId == requestedId
                    val isAuthorized = principal.role == "ADMIN"

                    if (!isAuthorized) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "⛔ Only admin can delete users or you can only delete your own account")
                        )
                        return@delete
                    }

                    val result = authService.deleteUserById(requestedId)
                    result.fold(
                        onSuccess = {
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf("message" to "🗑️ User deleted successfully")
                            )
                        },
                        onFailure = { error ->
                            val msg = error.message ?: "❌ Failed to delete user"
                            val status = if (msg.contains("not found", ignoreCase = true)) HttpStatusCode.NotFound
                            else HttpStatusCode.InternalServerError
                            // Cung cấp thông báo lỗi chung hơn
                            call.respond(status, mapOf("error" to "⚠️ Failed to delete user: $msg"))
                        }
                    )
                }
            }
        }

        // --- Thêm route mới để xem hồ sơ công khai theo username ---
        route("/api/users") {
            /**
             * 📌 Xem hồ sơ người dùng khác (GET /api/users/{username})
             * Lấy thông tin công khai của một người dùng bất kỳ dựa vào username.
             * Không yêu cầu xác thực.
             */
            get("/{username}") {
                val username = call.parameters["username"]
                if (username == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "❌ Username is required"))
                    return@get
                }

                val result = authService.getPublicProfileByUsername(username)
                result.fold(
                    onSuccess = { userProfile ->
                        call.respond(HttpStatusCode.OK, userProfile)
                    },
                    onFailure = {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                    }
                )
            }
        }
    }
}
