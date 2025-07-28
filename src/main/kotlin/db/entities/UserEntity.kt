package com.codewithngoc.instagallery.db.entities

import com.codewithngoc.instagallery.db.tables.UsersTable
import com.codewithngoc.instagallery.domain.models.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(UsersTable)
    var username by UsersTable.username
    var email by UsersTable.email
    var passwordHash by UsersTable.passwordHash
    var fullName by UsersTable.fullName
    var profilePictureUrl by UsersTable.profilePictureUrl
    var bio by UsersTable.bio
    var userType by UsersTable.userType
    var role by UsersTable.role
    var location by UsersTable.location
    var isVerified by UsersTable.isVerified
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
        postCount = 0,       // TODO: Query số bài viết
        followerCount = 0,   // TODO: Query số follower
        followingCount = 0   // TODO: Query số following
    )

    /**
     * Chuyển UserEntity sang response đơn giản
     */
    fun toUserSimpleResponse(): UserSimpleResponse = UserSimpleResponse(
        userId = id.value,
        username = username,
        profilePictureUrl = profilePictureUrl
    )
}
