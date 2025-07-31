package com.codewithngoc.instagallery.db.entities

import com.codewithngoc.instagallery.db.tables.UsersTable
import com.codewithngoc.instagallery.domain.models.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.Instant
import java.time.LocalDate

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(UsersTable)
    // ==== Basic info ====
    var username by UsersTable.username
    var email by UsersTable.email
    var passwordHash by UsersTable.passwordHash
    var fullName by UsersTable.fullName

    // ==== Profile info ====
    var profilePictureUrl by UsersTable.profilePictureUrl
    var bio by UsersTable.bio
    var website by UsersTable.website
    var gender by UsersTable.gender
    var phoneNumber by UsersTable.phoneNumber
    var dateOfBirth by UsersTable.dateOfBirth
    var location by UsersTable.location

    // ==== User type, role, verification ====
    var userType by UsersTable.userType
    var role by UsersTable.role
    var isVerified by UsersTable.isVerified

    // ==== Timestamp ====
    var createdAt by UsersTable.createdAt
    var updatedAt by UsersTable.updatedAt

    /**
     * Chuyển UserEntity sang domain model User
     */
    fun toUser(): User = User(
        userId = id.value,
        username = username,
        email = email,
        fullName = fullName,
        userType = userType.name,
        role = role.name
    )

    /**
     * Chuyển UserEntity sang profile response
     */
    fun toUserProfileResponse(): UserProfileResponse = UserProfileResponse(
        userId = id.value,
        username = username,
        email = email,
        fullName = fullName,
        profilePictureUrl = profilePictureUrl,
        bio = bio,
        location = location,
        isVerified = isVerified,
        postCount = 0,       // TODO: Implement query: PostsTable.select { post.userId eq id }.count()
        followerCount = 0,   // TODO: Implement query: FollowersTable.select { followedId eq id }.count()
        followingCount = 0   // TODO: Implement query: FollowersTable.select { followerId eq id }.count()
    )

    /**
     * Chuyển UserEntity sang response đơn giản
     */
    fun toUserSimpleResponse(): UserSimpleResponse = UserSimpleResponse(
        userId = id.value,
        username = username,
        profilePictureUrl = profilePictureUrl
    )

    /**
     * Cập nhật thông tin profile từ request
     */
    fun updateFrom(request: UpdateProfileRequest) {
        println("🔧 Updating UserEntity from request: $request")

        request.fullName?.let {
            println("➡️ Updating fullName: $it")
            fullName = it
        }
        request.profilePictureUrl?.let {
            println("➡️ Updating profilePictureUrl: $it")
            profilePictureUrl = it
        }
        request.bio?.let {
            println("➡️ Updating bio: $it")
            bio = it
        }
        request.location?.let {
            println("➡️ Updating location: $it")
            location = it
        }
        request.website?.let {
            println("➡️ Updating website: $it")
            website = it
        }
        request.gender?.let {
            println("➡️ Updating gender: $it")
            gender = it
        }
        request.phoneNumber?.let {
            println("➡️ Updating phoneNumber: $it")
            phoneNumber = it
        }
        request.dateOfBirth?.let {
            try {
                val parsedDate = LocalDate.parse(it)
                println("➡️ Updating dateOfBirth: $parsedDate")
                dateOfBirth = parsedDate
            } catch (e: Exception) {
                println("❌ Lỗi định dạng dateOfBirth: $it - ${e.message}")
            }
        }

        updatedAt = Instant.now()
        println("✅ updatedAt = $updatedAt")

        this.flush()
    }


}
