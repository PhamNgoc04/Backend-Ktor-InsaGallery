package com.codewithngoc.instagallery.domain.models

import com.codewithngoc.instagallery.db.tables.MediaType
import com.codewithngoc.instagallery.db.tables.PostVisibility
import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    val caption: String?,
    val visibility: PostVisibility = PostVisibility.PUBLIC,
    val location: String?,
    val mediaUrls: List<String>, // Client gửi lên danh sách URL sau khi upload xong
    val mediaTypes: List<MediaType> // "IMAGE" or "VIDEO"
)

@Serializable
data class PostMediaResponse(
    val id: Int,
    val mediaFileUrl: String,
    val mediaType: String,
    val position: Int
)

@Serializable
data class PostResponse(
    val id: Int,
    val caption: String?,
    val visibility: String,
    val location: String?,
    val media: List<PostMediaResponse>,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class TagResponse(
    val tagId: Int,
    val name: String
)

@Serializable
data class PostCreationResponse(
    val message: String,
    val post: PostResponse
)