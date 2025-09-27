package com.codewithngoc.instagallery.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class LikeResponse(
    val userId: Int,
    val username: String,
    val fullName: String,
    val profilePictureUrl: String?
)

@Serializable
data class PagedResponse<T>(
    val data: List<T>,
    val page: Int,
    val size: Int,
    val totalCount: Int,
    val totalPages: Int
)