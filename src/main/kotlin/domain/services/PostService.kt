package com.codewithngoc.instagallery.domain.services

import com.codewithngoc.instagallery.domain.models.CreatePostRequest
import com.codewithngoc.instagallery.domain.models.PostResponse

interface PostService {
    suspend fun createPost(userId: Int, request: CreatePostRequest): PostResponse
    suspend fun getPostDetail(postId: Int, viewerId: Int?): PostResponse?
    suspend fun updatePost(userId: Int, postId: Int, request: CreatePostRequest): PostResponse?
    suspend fun deletePost(userId: Int, postId: Int): Boolean
    suspend fun getNewsFeed(userId: Int, page: Int, size: Int): List<PostResponse>
    suspend fun getExplorePosts(page: Int, size: Int): List<PostResponse>
}