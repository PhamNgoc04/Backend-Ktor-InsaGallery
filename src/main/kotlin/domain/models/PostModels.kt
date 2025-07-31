package com.codewithngoc.instagallery.domain.models

import com.codewithngoc.instagallery.db.tables.MediaType
import com.codewithngoc.instagallery.db.tables.PostVisibility
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Post(
    val postId: Int,
    val userId : Int,
    val caption: String? = null,
    val location: String? = null,
    val visibility: PostVisibility = PostVisibility.PUBLIC,
    val likeCount: Int = 0, // Số lượng likes
    val commentCount: Int = 0, // Số lượng bình luận
)

@Serializable
data class MediaItem(
    val mediaFileUrl: String,
    val thumbnailUrl: String? = null,
    val mediaType: MediaType,
    val position: Int,
    val filterId: Int? = null,
    val metadata: String? = null
)

@Serializable
data class CreatePostRequest(
    val caption: String? = null,
    val visibility: PostVisibility = PostVisibility.PUBLIC,
    val location: String? = null,
    val media: List<MediaItem> // Danh sách media cho bài đăng
)

@Serializable
data class UpdatePostRequest(
    val caption: String? = null,
    val visibility: PostVisibility? = null,
    val location: String? = null,
    val media: List<MediaItem>? = null
)

@Serializable
data class MediaResponse(
    val mediaId: Int,
    val mediaFileUrl: String,
    val thumbnailUrl: String? = null,
    val mediaType: MediaType,
    val position: Int,
    val filterId: Int? = null,
    val metadata: String? = null
)

@Serializable
data class AuthorInfoResponse(
    val userId: Int,
    val username: String,
    val profilePictureUrl: String? = null
)

@Serializable
data class PostResponse(
    val postId: Int,
    val author: AuthorInfoResponse, // Thông tin tác giả
    val caption: String?,
    val location: String?,
    val visibility: PostVisibility,
    val media: List<MediaResponse>, // Danh sách media của bài đăng
    val likeCount: Int,
    val commentCount: Int,
)



