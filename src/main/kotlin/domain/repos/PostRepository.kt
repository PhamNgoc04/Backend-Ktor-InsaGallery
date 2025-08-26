package com.codewithngoc.instagallery.domain.repos

import com.codewithngoc.instagallery.db.entities.CommentEntity
import com.codewithngoc.instagallery.db.tables.PostVisibility
import com.codewithngoc.instagallery.domain.models.MediaItem
import com.codewithngoc.instagallery.domain.models.MediaResponse
import com.codewithngoc.instagallery.domain.models.Post

interface PostRepository {

    suspend fun createPost(userId: Int, caption: String?, visibility: PostVisibility, location: String?): Int // Trả về postId
    suspend fun addPostMedia(postId: Int, mediaItems: List<MediaItem>)
    suspend fun getPostById(postId: Int): Post? // Trả về domain model Post
    suspend fun updatePost(postId: Int, caption: String?, visibility: PostVisibility?, location: String?): Post?
    suspend fun deletePost(postId: Int): Boolean
    suspend fun getPostAuthorId(postId: Int): Int?
    suspend fun getPostVisibility(postId: Int): PostVisibility?
    suspend fun getFeedPosts(userId: Int, limit: Int, offset: Int): List<Post>
    suspend fun getExplorePosts(limit: Int, offset: Int): List<Post>
    suspend fun getMediaForPost(postId: Int): List<MediaResponse>
    suspend fun getPostLikeCount(postId: Int): Int
    suspend fun getPostCommentCount(postId: Int): Int
    suspend fun deleteAllPosts(): Boolean

    // Lấy tất cả bài viết của người dùng
    suspend fun getAllPosts(): List<Post>

    suspend fun deleteMediaForPost(postId: Int)

    suspend fun addMediaToPost(postId: Int, mediaItem: MediaItem)

    // ✅ Hàm mới: Thêm một bình luận
    suspend fun addComment(
        postId: Int,
        userId: Int,
        content: String,
        parentCommentId: Int?
    ): Int

    // ✅ Hàm mới: Lấy danh sách bình luận của một bài đăng
    suspend fun getCommentsForPost(
        postId: Int,
        page: Int,
        size: Int
    ): List<CommentEntity>

    // ✅ Hàm mới: Lấy bình luận bằng ID
    suspend fun getCommentById(commentId: Int): CommentEntity?

    // ✅ Cập nhật số lượng bình luận của bài viết
    suspend fun incrementCommentCount(postId: Int)

    suspend fun getUserPosts(userId: Int, page: Int, size: Int): List<Post>

}