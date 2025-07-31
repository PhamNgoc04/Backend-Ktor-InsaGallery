package com.codewithngoc.instagallery.db.entities

import com.codewithngoc.instagallery.db.tables.PostMediaTable
import com.codewithngoc.instagallery.domain.models.MediaResponse
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PostMediaEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PostMediaEntity>(PostMediaTable)

    var postId by PostMediaTable.postId.transform({ it.id }, { PostEntity[it] })
    var mediaFileUrl by PostMediaTable.mediaFileUrl
    var thumbnailUrl by PostMediaTable.thumbnailUrl
    var mediaType by PostMediaTable.mediaType
    var position by PostMediaTable.position
    var filterId by PostMediaTable.filterId
    var metadata by PostMediaTable.metadata

    fun toMediaResponse(): MediaResponse {
        return MediaResponse(
            mediaId = id.value,
            mediaFileUrl = mediaFileUrl,
            thumbnailUrl = thumbnailUrl,
            mediaType = mediaType,
            position = position,
            filterId = filterId?.value, // Lấy giá trị của EntityID
            metadata = metadata
        )
    }
}
