package com.codewithngoc.instagallery.routes

import com.codewithngoc.instagallery.config.generateToken
import com.codewithngoc.instagallery.domain.models.LoginRequest
import com.codewithngoc.instagallery.domain.models.RegisterRequest
import com.codewithngoc.instagallery.domain.models.AuthResponse
import com.codewithngoc.instagallery.domain.models.LoginResponse
import com.codewithngoc.instagallery.domain.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.authRoutes(authService: AuthService) {
    routing {
        route("/api/auth") {

            /**
             * 📌 Đăng ký tài khoản mới
             */
            post("/register") {
                val request = call.receive<RegisterRequest>()

                val user = authService.registerUser(request)
                if (user != null) {
                    // Tạo token JWT
                    call.respond(HttpStatusCode.Created, user)
                } else {
                    call.respond(HttpStatusCode.Conflict, "Username or Email already exists")
                }
            }

            /**
             * 📌 Đăng nhập
             */
            post("/login") {
                val request = call.receive<LoginRequest>()

                val user = authService.loginUser(request)
                if (user != null) {
                    val token = generateToken(user)
                    if (token != null) {
                        val res = LoginResponse(
                            userId = user.userId,
                            username = user.username,
                            token = token
                        )
                        call.respond(HttpStatusCode.OK, res)
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid Credentials")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid Credentials")
                }
            }

            /*
            Đăng xuất
             */
            authenticate("auth-jwt") {
                post("/logout") {
                    val userId = call.principal<UserIdPrincipal>()?.name?.toIntOrNull()
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                        return@post
                    }
                    // Xử lý logout (ví dụ xóa token khỏi blacklist nếu có)

                    call.respond(HttpStatusCode.OK, "Logged out successfully")
                }
            }

            /**
             * 📌 Lấy thông tin user theo ID (có thể dùng auth header kiểm tra)
             */
            authenticate("auth-jwt") {
                get("/profile/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid user ID format")
                        return@get
                    }

                    val profile = authService.getUserById(id)
                    if (profile != null) {
                        call.respond(HttpStatusCode.OK, profile)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "User not found")
                    }
                }
            }

            /**
             * 📌 Xóa user theo ID
             */
            authenticate("auth-jwt") {
                delete("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid user ID format")
                        return@delete
                    }

                    val wasDeleted = authService.deleteUserById(id)
                    if (wasDeleted) {
                        call.respond(HttpStatusCode.OK, "User deleted successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "User not found")
                    }
                }
            }
        }
    }
}
