package com.codewithngoc.instagallery.db.entities

import com.codewithngoc.instagallery.db.tables.PostsTable
import com.codewithngoc.instagallery.db.tables.PostMediaTable
import com.codewithngoc.instagallery.domain.models.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PostEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PostEntity>(PostsTable)

    var user by UserEntity referencedOn PostsTable.userId
    var caption by PostsTable.caption
    var location by PostsTable.location
    var visibility by PostsTable.visibility
    var createdAt by PostsTable.createdAt
    var updatedAt by PostsTable.updatedAt

    // Quan hệ 1-nhiều với media
    val medias by PostMediaEntity referrersOn PostMediaTable.postId

    /**
     * Chuyển PostEntity sang domain model PostResponse
     */
    fun PostEntity.toPostResponse(): PostResponse {
        return PostResponse(
            id = this.id.value,
            caption = this.caption,
            visibility = this.visibility.name,
            location = this.location,
            createdAt = this.createdAt.toString(), // ISO format
            updatedAt = this.updatedAt.toString(),
            media = this.medias.map { it.toPostMediaResponse() }
        )
    }

    fun PostMediaEntity.toPostMediaResponse(): PostMediaResponse {
        return PostMediaResponse(
            id = this.id.value,
            mediaFileUrl = this.mediaFileUrl,
            mediaType = this.mediaType.name,
            position = this.position
        )
    }

}
