package com.codewithngoc.instagallery.data.services

import com.codewithngoc.instagallery.db.entities.UserEntity
import com.codewithngoc.instagallery.db.tables.PostVisibility
import com.codewithngoc.instagallery.db.utils.dbQuery
import com.codewithngoc.instagallery.domain.models.AddCommentRequest
import com.codewithngoc.instagallery.domain.models.AuthPrincipal
import com.codewithngoc.instagallery.domain.models.AuthorInfoResponse
import com.codewithngoc.instagallery.domain.models.CommentResponse
import com.codewithngoc.instagallery.domain.models.CreatePostRequest
import com.codewithngoc.instagallery.domain.models.PostResponse
import com.codewithngoc.instagallery.domain.models.UpdatePostRequest
import com.codewithngoc.instagallery.domain.repos.PostRepository
import com.codewithngoc.instagallery.domain.services.PostService
import org.jetbrains.exposed.sql.exposedLogger

class PostServiceImpl(
    private val postRepository: PostRepository
) : PostService {
    override suspend fun createPost(userId: Int, request: CreatePostRequest): Result<PostResponse> {
        return try {
            val postId = postRepository.createPost(
                userId = userId,
                caption = request.caption,
                visibility = request.visibility,
                location = request.location
            )

            postRepository.addPostMedia(postId, request.media)

            val post = postRepository.getPostById(postId)
                ?: return Result.failure(Exception("Post not found"))

            val media = postRepository.getMediaForPost(postId)

            val authorEntity = dbQuery {
                UserEntity.findById(userId)
            } ?: return Result.failure(Exception("User not found"))


            val author = AuthorInfoResponse(
                userId = authorEntity.id.value,
                username = authorEntity.username,
                profilePictureUrl = authorEntity.profilePictureUrl
            )

            val postResponse = PostResponse(
                postId = post.postId,
                author = author,
                caption = post.caption,
                location = post.location,
                visibility = post.visibility,
                media = media,
                likeCount = post.likeCount,
                commentCount = post.commentCount,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt
            )

            Result.success(postResponse)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getPostById(postId: Int, principal: AuthPrincipal?): Result<PostResponse> {
        return try {
            val post = postRepository.getPostById(postId)
                ?: return Result.failure(Exception("Post not found"))

            val canView = post.visibility == PostVisibility.PUBLIC || principal?.userId == post.userId
            if (!canView) return Result.failure(Exception("Access denied"))

            val media = postRepository.getMediaForPost(postId)

            val authorEntity = dbQuery { UserEntity.findById(post.userId) }
                ?: return Result.failure(Exception("Author not found"))

            val author = AuthorInfoResponse(
                userId = authorEntity.id.value,
                username = authorEntity.username,
                profilePictureUrl = authorEntity.profilePictureUrl
            )

            Result.success(
                PostResponse(
                    postId = post.postId,
                    author = author,
                    caption = post.caption,
                    location = post.location,
                    visibility = post.visibility,
                    media = media,
                    likeCount = post.likeCount,
                    commentCount = post.commentCount,
                    createdAt = post.createdAt,
                    updatedAt = post.updatedAt
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updatePost(
        postId: Int,
        principal: AuthPrincipal,
        request: UpdatePostRequest
    ): Result<PostResponse> {
        return try {
            // 1. Kiểm tra quyền sở hữu
            val authorId = postRepository.getPostAuthorId(postId)
                ?: return Result.failure(Exception("Post not found"))

            if (authorId != principal.userId) {
                return Result.failure(Exception("Unauthorized"))
            }

            // 2. Cập nhật thông tin cơ bản của bài viết
            val updatedPost = postRepository.updatePost(
                postId = postId,
                caption = request.caption,
                visibility = request.visibility,
                location = request.location
            ) ?: return Result.failure(Exception("Failed to update post"))

            // 3. Nếu có media mới, xoá toàn bộ media cũ và thêm lại
            if (request.media != null) {
                postRepository.deleteMediaForPost(postId)
                request.media.forEach { mediaItem ->
                    postRepository.addMediaToPost(postId, mediaItem)
                }
            }

            // 4. Lấy danh sách media mới nhất
            val media = postRepository.getMediaForPost(postId)

            // 5. Lấy thông tin tác giả
            val authorEntity = dbQuery { UserEntity.findById(updatedPost.userId) }
                ?: return Result.failure(Exception("Author not found"))

            val author = AuthorInfoResponse(
                userId = authorEntity.id.value,
                username = authorEntity.username,
                profilePictureUrl = authorEntity.profilePictureUrl
            )

            // 6. Trả về kết quả
            Result.success(
                PostResponse(
                    postId = updatedPost.postId,
                    author = author,
                    caption = updatedPost.caption,
                    location = updatedPost.location,
                    visibility = updatedPost.visibility,
                    media = media,
                    likeCount = updatedPost.likeCount,
                    commentCount = updatedPost.commentCount,
                    createdAt = updatedPost.createdAt,
                    updatedAt = updatedPost.updatedAt
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }


    override suspend fun deletePost(postId: Int, principal: AuthPrincipal): Result<Boolean> {
        return try {
            val authorId = postRepository.getPostAuthorId(postId)
                ?: return Result.failure(Exception("Post not found"))

            if (authorId != principal.userId) return Result.failure(Exception("Unauthorized"))

            val success = postRepository.deletePost(postId)

            if (success) Result.success(true)
            else Result.failure(Exception("Failed to delete post"))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getFeedPosts(
        userId: Int,
        page: Int,
        size: Int
    ): Result<List<PostResponse>> {
        return try {
            val offset = page * size
            val posts = postRepository.getFeedPosts(userId, size, offset)

            val responses = posts.map { post ->
                val media = postRepository.getMediaForPost(post.postId)
                val authorEntity = dbQuery { UserEntity.findById(post.userId) }
                    ?: throw Exception("Author not found for post ID: ${post.postId}")

                val author = AuthorInfoResponse(
                    userId = authorEntity.id.value,
                    username = authorEntity.username,
                    profilePictureUrl = authorEntity.profilePictureUrl
                )

                PostResponse(
                    postId = post.postId,
                    author = author,
                    caption = post.caption,
                    location = post.location,
                    visibility = post.visibility,
                    media = media,
                    likeCount = post.likeCount,
                    commentCount = post.commentCount,
                    createdAt = post.createdAt,
                    updatedAt = post.updatedAt
                )
            }
            Result.success(responses)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getExplorePosts(page: Int, size: Int): Result<List<PostResponse>> {
        return try {
            val offset = page * size
            val posts = postRepository.getExplorePosts(size, offset)

            val responses = posts.map { post ->
                val media = postRepository.getMediaForPost(post.postId)
                val authorEntity = dbQuery { UserEntity.findById(post.userId) }
                    ?: throw Exception("Author not found for post ID: ${post.postId}")

                val author = AuthorInfoResponse(
                    userId = authorEntity.id.value,
                    username = authorEntity.username,
                    profilePictureUrl = authorEntity.profilePictureUrl
                )

                PostResponse(
                    postId = post.postId,
                    author = author,
                    caption = post.caption,
                    location = post.location,
                    visibility = post.visibility,
                    media = media,
                    likeCount = post.likeCount,
                    commentCount = post.commentCount,
                    createdAt = post.createdAt,
                    updatedAt = post.updatedAt
                )
            }
            Result.success(responses)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteAllPosts(principal: AuthPrincipal): Result<Boolean> {
        return try {
            // Kiểm tra quyền của người dùng
            if (principal.role != "ADMIN") {
                val error = Result.failure<Boolean>(Exception("Unauthorized"))
                return error
            }

            val success = postRepository.deleteAllPosts()
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete all posts"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getAllPosts(): Result<List<PostResponse>> {
        return try {
            val posts = postRepository.getAllPosts()

            val responses = posts.map { post ->
                val media = postRepository.getMediaForPost(post.postId)
                val authorEntity = dbQuery { UserEntity.findById(post.userId) }
                    ?: throw Exception("Author not found for post ID: ${post.postId}")

                val author = AuthorInfoResponse(
                    userId = authorEntity.id.value,
                    username = authorEntity.username,
                    profilePictureUrl = authorEntity.profilePictureUrl
                )

                PostResponse(
                    postId = post.postId,
                    author = author,
                    caption = post.caption,
                    location = post.location,
                    visibility = post.visibility,
                    media = media,
                    likeCount = post.likeCount,
                    commentCount = post.commentCount,
                    createdAt = post.createdAt,
                    updatedAt = post.updatedAt
                )
            }
            Result.success(responses)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun addComment(
        postId: Int,
        userId: Int,
        request: AddCommentRequest
    ): Result<CommentResponse> {
        return try {
            val commentId = postRepository.addComment(
                postId = postId,
                userId = userId,
                content = request.content,
                parentCommentId = request.parentCommentId
            )

            // Tăng số lượng bình luận trong bảng posts
            postRepository.incrementCommentCount(postId)

            val newComment = postRepository.getCommentById(commentId)
                ?: return Result.failure(Exception("Comment not found"))

            val authorEntity = dbQuery { UserEntity.findById(userId) }
                ?: return Result.failure(Exception("User not found"))

            val authorInfo = AuthorInfoResponse(
                userId = authorEntity.id.value,
                username = authorEntity.username,
                profilePictureUrl = authorEntity.profilePictureUrl
            )

            // TODO: Tạo thông báo cho chủ bài viết (và chủ bình luận cha)

            Result.success(newComment.toCommentResponse(authorInfo))

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getCommentsForPost(
        postId: Int,
        page: Int,
        size: Int
    ): Result<List<CommentResponse>> {
        return try {
            val commentEntities = postRepository.getCommentsForPost(postId, page, size)
            val comments = commentEntities.map { commentEntity ->
                val authorEntity = dbQuery { UserEntity.findById(commentEntity.userId.value) }
                    ?: throw Exception("Author not found for comment ID: ${commentEntity.id.value}")

                val authorInfo = AuthorInfoResponse(
                    userId = authorEntity.id.value,
                    username = authorEntity.username,
                    profilePictureUrl = authorEntity.profilePictureUrl
                )
                commentEntity.toCommentResponse(authorInfo)
            }

            // TODO: Sắp xếp lại bình luận theo cấu trúc cha-con

            Result.success(comments)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getUserPosts(userId: Int, page: Int, size: Int): Result<List<PostResponse>> {
        return try {
            // ✅ Pass 'page' and 'size' directly to the repository
            val posts = postRepository.getUserPosts(userId, page, size)

            val responses = posts.map { post ->
                val media = postRepository.getMediaForPost(post.postId) // Use post.id
                val authorEntity = dbQuery { UserEntity.findById(post.userId) }
                    ?: throw Exception("Author not found for post ID: ${post.postId}")

                val author = AuthorInfoResponse(
                    userId = authorEntity.id.value,
                    username = authorEntity.username,
                    profilePictureUrl = authorEntity.profilePictureUrl
                )

                // Lấy số liệu thống kê bài viết
                val likeCount = postRepository.getPostLikeCount(post.postId) // Use post.id
                val commentCount = postRepository.getPostCommentCount(post.postId) // Use post.id

                PostResponse(
                    postId = post.postId, // Use post.id
                    author = author,
                    caption = post.caption,
                    location = post.location,
                    visibility = post.visibility,
                    media = media,
                    likeCount = likeCount,
                    commentCount = commentCount,
                    createdAt = post.createdAt,
                    updatedAt = post.updatedAt
                )
            }
            Result.success(responses)
        } catch (e: Exception) {
            exposedLogger.error("Error getting posts for user $userId: ${e.message}", e)
            Result.failure(e)
        }
    }


}
