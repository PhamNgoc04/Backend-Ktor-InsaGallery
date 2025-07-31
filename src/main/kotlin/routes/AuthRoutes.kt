package com.codewithngoc.instagallery.routes

import com.codewithngoc.instagallery.domain.models.AuthPrincipal
import com.codewithngoc.instagallery.domain.models.LoginRequest
import com.codewithngoc.instagallery.domain.models.RegisterRequest
import com.codewithngoc.instagallery.domain.models.UpdateProfileRequest
import com.codewithngoc.instagallery.domain.services.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
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
        route("/api/auth") {
            /**
             * 📌 Đăng ký tài khoản mới
             */
            post("/register") {
                val request = call.receive<RegisterRequest>()

                val result = authService.registerUser(request)
                result.fold(
                    onSuccess = { authResponse ->
                        call.respond(HttpStatusCode.Created, authResponse)
                    },
                    onFailure = { error ->
                        call.respond(HttpStatusCode.Conflict, error.message ?: "Register failed")
                    }
                )
            }

            /**
             * 📌 Đăng nhập
             */
            post("/login") {
                val request = call.receive<LoginRequest>()

                val result = authService.loginUser(request)
                result.fold(
                    onSuccess = { loginResponse ->
                        call.respond(HttpStatusCode.OK, loginResponse)
                    },
                    onFailure = { error ->
                        call.respond(HttpStatusCode.Unauthorized, error.message ?: "Login failed")
                    }
                )
            }
            authenticate("auth-jwt") {

                /**
                 * 📌 Lấy thông tin user theo ID (có thể dùng auth header kiểm tra)
                 * User và Admin đều được truy cập, nhưng User chỉ có thể lấy thông tin của chính mình.
                 * Admin có thể lấy thông tin của bất kỳ user nào.
                 */
                get("/profile/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid user ID format")
                        return@get
                    }

                    val principal = call.principal<AuthPrincipal>()
                    if (principal == null || (principal.role != "ADMIN" && principal.userId != id)) {
                        // Kiểm tra quyền truy cập
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "⛔ Permission denied"))
                        return@get
                    }

                    val result = authService.getUserById(id)
                    result.fold(
                        onSuccess = { call.respond(HttpStatusCode.OK, it) },
                        onFailure = { call.respond(HttpStatusCode.NotFound, it.message ?: "User not found") }
                    )
                }


                /**
                 * 📌 Cập nhật thông tin người dùng
                 * User và Admin đều được truy cập, nhưng User chỉ có thể cập nhật thông tin của chính mình.
                 * Admin có thể cập nhật thông tin của bất kỳ user nào.
                 */
                put("/profile/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "❌ Invalid user ID format"))
                        return@put
                    }

                    val principal = call.principal<AuthPrincipal>()
                    if (principal == null || (principal.role != "ADMIN" && principal.userId != id)) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "⛔ Permission denied"))
                        return@put
                    }

                    val updateRequest = try {
                        call.receive<UpdateProfileRequest>()
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "❌ Invalid request body"))
                        return@put
                    }

                    val result = authService.updateUserProfile(id, updateRequest)
                    result.fold(
                        onSuccess = { call.respond(HttpStatusCode.OK, it) },
                        onFailure = { call.respond(HttpStatusCode.NotFound, it.message ?: "Update failed") }
                    )
                }

                /**
                 * 🗑️ Xóa người dùng theo ID
                 */
                delete("/profile/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "❌ Invalid user ID format")
                        )
                        return@delete
                    }

                    val principal = call.principal<AuthPrincipal>()
                    if (principal?.role != "ADMIN") {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "⛔ Only admin can delete users")
                        )
                        return@delete
                    }

                    val result = authService.deleteUserById(id)
                    result.fold(
                        onSuccess = {
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf("message" to "🗑️ User deleted successfully")
                            )
                        },
                        onFailure = { error ->
                            val msg = error.message ?: "❌ Failed to delete user"
                            val status = if (msg.contains(
                                    "not found",
                                    ignoreCase = true
                                )
                            ) HttpStatusCode.NotFound
                            else HttpStatusCode.InternalServerError

                            call.respond(status, mapOf("error" to "⚠️ $msg"))
                        }
                    )
                }
            }
        }
    }
}