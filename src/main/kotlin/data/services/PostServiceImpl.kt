package com.codewithngoc.instagallery.data.services

import com.codewithngoc.instagallery.domain.models.CreatePostRequest
import com.codewithngoc.instagallery.domain.models.PostResponse
import com.codewithngoc.instagallery.domain.repos.PostRepository
import com.codewithngoc.instagallery.domain.services.PostService

class PostServiceImpl(
    private val postRepository: PostRepository
) : PostService {

    override suspend fun createPost(userId: Int, request: CreatePostRequest): PostResponse {
        return postRepository.insertPost(userId, request)
    }


    override suspend fun getPostDetail(postId: Int, viewerId: Int?): PostResponse? {
        return postRepository.fetchPostDetail(postId, viewerId)
    }

    override suspend fun updatePost(userId: Int, postId: Int, request: CreatePostRequest): PostResponse? {
        // Kiểm tra quyền (đơn giản ở đây lấy luôn) hoặc trong repository
        val success = postRepository.updatePost(userId, postId, request)
        return if (success) postRepository.fetchPostDetail(postId, userId) else null
    }

    override suspend fun deletePost(userId: Int, postId: Int): Boolean {
        return postRepository.deletePost(postId, userId)
    }

    override suspend fun getNewsFeed(userId: Int, page: Int, size: Int): List<PostResponse> {
        return postRepository.fetchNewsFeed(userId, page, size)
    }

    override suspend fun getExplorePosts(page: Int, size: Int): List<PostResponse> {
        return postRepository.fetchExplorePosts(page, size)
    }
}

