package com.codewithngoc.instagallery.domain.repos

import com.codewithngoc.instagallery.domain.models.CreatePostRequest
import com.codewithngoc.instagallery.domain.models.PostResponse

interface PostRepository {
    suspend fun insertPost(userId: Int, request: CreatePostRequest): PostResponse
    suspend fun insertPostMedia(postId: Int, request: CreatePostRequest)
    suspend fun fetchPostDetail(postId: Int, viewerId: Int?): PostResponse?
    suspend fun updatePost(userId: Int,postId: Int, request: CreatePostRequest): Boolean
    suspend fun deletePost(postId: Int, userId: Int): Boolean
    suspend fun fetchNewsFeed(userId: Int, page: Int, size: Int): List<PostResponse>
    suspend fun fetchExplorePosts(page: Int, size: Int): List<PostResponse>
}
