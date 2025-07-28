package com.codewithngoc.instagallery.db.entities

import com.codewithngoc.instagallery.db.tables.PostMediaTable
import com.codewithngoc.instagallery.domain.models.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PostMediaEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PostMediaEntity>(PostMediaTable)

    var post by PostEntity referencedOn PostMediaTable.postId
    var mediaFileUrl by PostMediaTable.mediaFileUrl
    var thumbnailUrl by PostMediaTable.thumbnailUrl
    var mediaType by PostMediaTable.mediaType
    var position by PostMediaTable.position
    var metadata by PostMediaTable.metadata

    /**
     * Chuyển PostMediaEntity sang domain model PostMediaResponse
     */
    fun toPostMediaResponse(): PostMediaResponse = PostMediaResponse(
        id = this.id.value,
        mediaFileUrl = this.mediaFileUrl,
        mediaType = this.mediaType.name,
        position = this.position
    )
}
