package com.codewithngoc.instagallery.domain.models

import com.codewithngoc.instagallery.db.entities.PostEntity
import com.codewithngoc.instagallery.db.entities.PostMediaEntity
import com.codewithngoc.instagallery.domain.models.PostResponse
import com.codewithngoc.instagallery.domain.models.PostMediaResponse

fun PostEntity.toPostResponse(): PostResponse = PostResponse(
    id = this.id.value,
    caption = this.caption,
    visibility = this.visibility.name,
    location = this.location,
    createdAt = this.createdAt.toString(),
    updatedAt = this.updatedAt.toString(),
    media = this.medias.map { it.toPostMediaResponse() }
)

fun PostMediaEntity.toPostMediaResponse(): PostMediaResponse = PostMediaResponse(
    id = this.id.value,
    mediaFileUrl = this.mediaFileUrl,
    mediaType = this.mediaType.name,
    position = this.position
)
