package com.codewithngoc.instagallery.domain.services

import com.codewithngoc.instagallery.domain.models.*
import kotlin.Result

interface PostService {
    // 📌 Tạo bài viết mới từ người dùng đã xác thực
    suspend fun createPost(userId: Int, request: CreatePostRequest): Result<PostResponse>

    // 🔍 Lấy bài viết theo ID, có thể kiểm tra quyền truy cập dựa trên AuthPrincipal
    suspend fun getPostById(postId: Int, principal: AuthPrincipal?): Result<PostResponse>

    // ✏️ Cập nhật bài viết nếu chủ sở hữu hợp lệ
    suspend fun updatePost(postId: Int, principal: AuthPrincipal, request: UpdatePostRequest): Result<PostResponse>

    // 🗑️ Xoá bài viết nếu người yêu cầu là tác giả
    suspend fun deletePost(postId: Int, principal: AuthPrincipal): Result<Boolean>

    // 📰 Lấy danh sách bài viết của người dùng và những người họ theo dõi (news feed)
    suspend fun getFeedPosts(userId: Int, page: Int, size: Int): Result<List<PostResponse>>

    // 🔥 Lấy danh sách bài viết công khai (explore feed)
    suspend fun getExplorePosts(page: Int, size: Int): Result<List<PostResponse>>

    // Xóa tất cả bài viết của người dùng (dùng trong quá trình xóa tài khoản)
    suspend fun deleteAllPosts(principal: AuthPrincipal): Result<Boolean>
}
