package com.codewithngoc.instagallery.data.repos

import com.codewithngoc.instagallery.db.entities.UserEntity
import com.codewithngoc.instagallery.db.tables.Role
import com.codewithngoc.instagallery.db.tables.UserSessionsTable
import com.codewithngoc.instagallery.db.tables.UserType
import com.codewithngoc.instagallery.db.tables.UsersTable
import com.codewithngoc.instagallery.db.utils.dbQuery
import com.codewithngoc.instagallery.domain.models.*
import com.codewithngoc.instagallery.domain.repos.AuthRepository
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.mindrot.jbcrypt.BCrypt

class AuthRepositoryImpl : AuthRepository {

    override suspend fun registerUser(request: RegisterRequest): User? {
        return dbQuery {
            val existingUser = UserEntity.find {
                (UsersTable.username eq request.username) or
                        (UsersTable.email eq request.email)
            }.firstOrNull()

            if (existingUser != null) {
                return@dbQuery null // User đã tồn tại
            }

            // Tạo user mới
            val newUser = UserEntity.new {
                username = request.username
                email = request.email
                passwordHash = hashPassword(request.password) // Băm mật khẩu
                fullName = request.fullName
                userType = UserType.valueOf(request.userType.uppercase())
                role = if (request.role != null) Role.valueOf(request.role.uppercase()) else Role.USER
                createdAt = java.time.Instant.now()
                updatedAt = java.time.Instant.now()
            }

            newUser.toUser()
        }
    }

    override suspend fun loginUser(request: LoginRequest): User? {
        return dbQuery {
            val userEntity = UserEntity.find { UsersTable.email eq request.email }.firstOrNull()
                ?: return@dbQuery null

            if (verifyPassword(request.password, userEntity.passwordHash)) {
                userEntity.toUser()
            } else {
                null
            }
        }
    }

    override suspend fun getUserById(id: Int): User? {
        return dbQuery {
            UserEntity.findById(id)?.toUser()
        }
    }

    override suspend fun deleteUserById(id: Int): Boolean {
        return dbQuery {
            val deletedRows = UsersTable.deleteWhere { UsersTable.id eq id }
            deletedRows > 0
        } == true
    }

    override suspend fun logout(refreshToken: String): Boolean {
        return dbQuery {
            val deletedRows = UserSessionsTable.deleteWhere {
                UserSessionsTable.refreshToken eq refreshToken
            }
            deletedRows > 0
        } == true
    }

    override suspend fun authenticate(request: LoginRequest): User? {
        return loginUser(request)
    }

    override fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    override fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(plainPassword, hashedPassword)
    }
}
