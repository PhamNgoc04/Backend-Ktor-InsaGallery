package com.codewithngoc.instagallery.data.services

import com.codewithngoc.instagallery.domain.models.AuthPrincipal
import com.codewithngoc.instagallery.domain.models.LikeResponse
import com.codewithngoc.instagallery.domain.models.PagedResponse
import com.codewithngoc.instagallery.domain.repos.LikeRepository
import com.codewithngoc.instagallery.domain.services.LikeService

class LikeServiceImpl(
    private val likeRepository: LikeRepository
) : LikeService {

    override suspend fun likePost(postId: Int, principal: AuthPrincipal): Result<Boolean> {
        return runCatching {
            val success = likeRepository.likePost(principal.userId, postId)
            if (!success) throw IllegalStateException("Already liked")
            true
        }
    }

    override suspend fun unlikePost(postId: Int, principal: AuthPrincipal): Result<Boolean> {
        return runCatching {
            val success = likeRepository.unlikePost(principal.userId, postId)
            if (!success) throw IllegalStateException("Not liked yet")
            true
        }
    }

    override suspend fun getLikesForPost(postId: Int, page: Int, size: Int): Result<PagedResponse<LikeResponse>> {
        return runCatching {
            val (likes, totalCount) = likeRepository.getLikesForPost(postId, page, size)
            val totalPages = if (totalCount == 0) 1 else (totalCount + size - 1) / size

            PagedResponse(
                data = likes,
                page = page,
                size = size,
                totalCount = totalCount,
                totalPages = totalPages
            )
        }
    }

    override suspend fun hasUserLiked(postId: Int, principal: AuthPrincipal): Result<Boolean> {
        return runCatching { likeRepository.hasUserLikedPost(principal.userId, postId) }
    }

    override suspend fun countLikes(postId: Int): Result<Int> {
        return runCatching {
            likeRepository.countLikes(postId)
        }
    }
}
