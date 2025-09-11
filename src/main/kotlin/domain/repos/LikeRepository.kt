package com.codewithngoc.instagallery.domain.repos

import com.codewithngoc.instagallery.domain.models.LikeResponse
import com.codewithngoc.instagallery.domain.models.PagedResponse

interface LikeRepository {
    suspend fun likePost(userId: Int, postId: Int): Boolean

    suspend fun unlikePost(userId: Int, postId: Int): Boolean

    suspend fun hasUserLikedPost(userId: Int, postId: Int): Boolean

    suspend fun getLikesForPost(postId: Int, page: Int, size: Int): Pair<List<LikeResponse>, Int>

    suspend fun countLikes(postId: Int): Int
}