package com.codewithngoc.instagallery.domain.services

import com.codewithngoc.instagallery.domain.models.AuthPrincipal
import com.codewithngoc.instagallery.domain.models.LikeResponse
import com.codewithngoc.instagallery.domain.models.PagedResponse

interface LikeService {
    suspend fun likePost(postId: Int, principal: AuthPrincipal): Result<Boolean>

    suspend fun unlikePost(postId: Int, principal: AuthPrincipal): Result<Boolean>

    suspend fun getLikesForPost(postId: Int, page: Int, size: Int): Result<PagedResponse<LikeResponse>>

    suspend fun hasUserLiked(postId: Int, principal: AuthPrincipal): Result<Boolean>

    suspend fun countLikes(postId: Int): Result<Int>

}