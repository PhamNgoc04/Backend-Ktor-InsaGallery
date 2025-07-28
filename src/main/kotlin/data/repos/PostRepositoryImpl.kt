package com.codewithngoc.instagallery.data.repos

import com.codewithngoc.instagallery.db.entities.PostEntity
import com.codewithngoc.instagallery.db.entities.PostMediaEntity
import com.codewithngoc.instagallery.db.entities.UserEntity
import com.codewithngoc.instagallery.db.tables.MediaType
import com.codewithngoc.instagallery.db.tables.PostMediaTable
import com.codewithngoc.instagallery.db.tables.PostVisibility
import com.codewithngoc.instagallery.db.tables.PostsTable
import com.codewithngoc.instagallery.domain.models.CreatePostRequest
import com.codewithngoc.instagallery.domain.models.PostResponse
import com.codewithngoc.instagallery.domain.models.toPostResponse
import com.codewithngoc.instagallery.domain.repos.PostRepository
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class PostRepositoryImpl : PostRepository {

    override suspend fun insertPost(userId: Int, request: CreatePostRequest): PostResponse =
        newSuspendedTransaction {
            val post = PostEntity.new {
                this.user = UserEntity[userId]
                this.caption = request.caption
                this.location = request.location
                this.visibility = request.visibility
                this.createdAt = Instant.now()
                this.updatedAt = Instant.now()
            }

            request.mediaUrls.forEachIndexed { index, mediaUrl ->
                val mediaType = request.mediaTypes.getOrNull(index)
                    ?: throw IllegalArgumentException("Missing media type for media index $index")

                PostMediaEntity.new {
                    this.post = post
                    this.mediaFileUrl = mediaUrl
                    this.thumbnailUrl = null
                    this.mediaType = mediaType
                    this.position = index
                    this.metadata = null
                }
            }

            post.toPostResponse()
        }

    override suspend fun insertPostMedia(postId: Int, request: CreatePostRequest) =
        newSuspendedTransaction {
            val post = PostEntity.findById(postId) ?: return@newSuspendedTransaction

            request.mediaUrls.forEachIndexed { index, mediaUrl ->
                val mediaType = request.mediaTypes.getOrNull(index)
                    ?: throw IllegalArgumentException("Missing media type for media index $index")

                PostMediaEntity.new {
                    this.post = post
                    this.mediaFileUrl = mediaUrl
                    this.thumbnailUrl = null
                    this.mediaType = mediaType
                    this.position = index
                    this.metadata = null
                }
            }
        }

    override suspend fun fetchPostDetail(postId: Int, viewerId: Int?): PostResponse? =
        newSuspendedTransaction {
            PostEntity.findById(postId)?.toPostResponse()
        }

    override suspend fun updatePost(userId: Int,postId: Int, request: CreatePostRequest): Boolean =
        newSuspendedTransaction {
            val post = PostEntity.findById(postId) ?: return@newSuspendedTransaction false
            if (post.user.id.value != userId) return@newSuspendedTransaction false // Kiểm tra quyền

            post.caption = request.caption
            post.location = request.location
            post.visibility = request.visibility
            post.updatedAt = Instant.now()

            PostMediaEntity.find { PostMediaTable.postId eq postId }
                .forEach { it.delete() }

            request.mediaUrls.forEachIndexed { index, mediaUrl ->
                val mediaType = request.mediaTypes.getOrNull(index)
                    ?: throw IllegalArgumentException("Missing media type for media index $index")

                PostMediaEntity.new {
                    this.post = post
                    this.mediaFileUrl = mediaUrl
                    this.thumbnailUrl = null
                    this.mediaType = mediaType
                    this.position = index
                    this.metadata = null
                }
            }
            true
        }

    override suspend fun deletePost(postId: Int, userId: Int): Boolean =
        newSuspendedTransaction {
            val post = PostEntity.findById(postId) ?: return@newSuspendedTransaction false
            if (post.user.id.value != userId) return@newSuspendedTransaction false
            post.delete()
            true
        }

    override suspend fun fetchNewsFeed(userId: Int, page: Int, size: Int): List<PostResponse> =
        newSuspendedTransaction {
            val safePage = page.coerceAtLeast(1)
            val safeSize = size.coerceIn(1, 100)

            PostEntity.find {
                (PostsTable.visibility eq PostVisibility.PUBLIC) or (PostsTable.userId eq userId)
            }.orderBy(PostsTable.createdAt to org.jetbrains.exposed.sql.SortOrder.DESC)
                .limit(safeSize, offset = ((safePage - 1) * safeSize).toLong())
                .map { it.toPostResponse() }
        }

    override suspend fun fetchExplorePosts(page: Int, size: Int): List<PostResponse> =
        newSuspendedTransaction {
            val safePage = page.coerceAtLeast(1)
            val safeSize = size.coerceIn(1, 100)

            PostEntity.find {
                PostsTable.visibility eq PostVisibility.PUBLIC
            }.orderBy(PostsTable.createdAt to org.jetbrains.exposed.sql.SortOrder.DESC)
                .limit(safeSize, offset = ((safePage - 1) * safeSize).toLong())
                .map { it.toPostResponse() }
        }
}
