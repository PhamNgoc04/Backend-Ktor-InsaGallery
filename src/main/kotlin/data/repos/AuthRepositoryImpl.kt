package com.codewithngoc.instagallery.data.repos

import com.codewithngoc.instagallery.db.entities.UserEntity
import com.codewithngoc.instagallery.db.tables.Role
import com.codewithngoc.instagallery.db.tables.UserSessionsTable
import com.codewithngoc.instagallery.db.tables.UserType
import com.codewithngoc.instagallery.db.tables.UsersTable
import com.codewithngoc.instagallery.db.utils.PasswordUtils
import com.codewithngoc.instagallery.db.utils.dbQuery
import com.codewithngoc.instagallery.domain.models.*
import com.codewithngoc.instagallery.domain.repos.AuthRepository
import io.ktor.client.request.request
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant



class AuthRepositoryImpl : AuthRepository {

    // ✅ Tạo người dùng mới (hash password trước khi gọi hàm này)
    override suspend fun registerUser(registerRequest: RegisterRequest): User? {
        return dbQuery {

            val existingUser = UserEntity.find {
                (UsersTable.username eq registerRequest.username) or
                        (UsersTable.email eq registerRequest.email)
            }.firstOrNull()

            if (existingUser != null) {
                return@dbQuery null // User đã tồn tại
            }

            // Tạo user mới
            val newUser = UserEntity.new {
                username = registerRequest.username
                email = registerRequest.email
                passwordHash = PasswordUtils.hashPassword(registerRequest.password) // Băm mật khẩu
                fullName = registerRequest.fullName
                userType = UserType.CLIENT
                role = Role.USER
                createdAt = Instant.now()
                updatedAt = Instant.now()
            }

            newUser.toUser()
        }
    }

    // ✅ Xác thực đăng nhập → trả về User nếu đúng
    override suspend fun loginUser(request: LoginRequest): User? {
        return dbQuery {
            val userEntity = UserEntity.find { UsersTable.email eq request.email }.firstOrNull()
                ?: return@dbQuery null

            if (PasswordUtils.verifyPassword(request.password, userEntity.passwordHash)) {
                userEntity.toUser()
            } else {
                null
            }
        }
    }

    // ✅ Lấy thông tin người dùng theo ID
    override suspend fun getUserById(userId: Int): UserEntity? {
        return try {
            dbQuery {
                UserEntity.findById(userId)
            }
        } catch (e: NumberFormatException) {
            null
        }
    }

    override suspend fun authenticate(request: LoginRequest): User? {
        TODO("Not yet implemented")
    }

    // ✅ Xóa người dùng theo ID
    override suspend fun deleteUserById(id: Int): Boolean {
        return dbQuery {
            val userEntity = UserEntity.findById(id) ?: return@dbQuery false

            // Xoá tất cả phiên đăng nhập của người dùng
            UserSessionsTable.deleteWhere { UserSessionsTable.userId eq id }

            // Xoá người dùng
            userEntity.delete()
            true
        }
    }

    // ✅ Đăng xuất
    override suspend fun logout(refreshToken: String): Boolean {
        return dbQuery {
            // Xoá phiên đăng nhập dựa trên refresh token
            val deletedRows = UserSessionsTable.deleteWhere { UserSessionsTable.refreshToken eq refreshToken }
            deletedRows > 0 // Trả về true nếu xoá thành công ít nhất 1 phiên
        }
    }

    // ✅ Cập nhật thông tin người dùng
    override suspend fun updateUserProfile(userId: Int, updateRequest: UpdateProfileRequest): User? {
        return dbQuery {
            val userEntity = UserEntity.findById(userId)
                ?: return@dbQuery null

            userEntity.updateFrom(updateRequest)
            userEntity.toUser()
        }
    }

    // ✅ Lấy thông tin công khai của người dùng theo username
    override suspend fun getPublicProfileByUsername(username: String): User? {
        return dbQuery {
            UserEntity.find { UsersTable.username eq username }
                .firstOrNull()
                ?.toUser()
        }
    }
}
