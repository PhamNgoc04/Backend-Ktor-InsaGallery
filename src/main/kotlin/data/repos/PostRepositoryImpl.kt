package com.codewithngoc.instagallery.data.repos

import com.codewithngoc.instagallery.db.entities.CommentEntity
import com.codewithngoc.instagallery.db.entities.PostEntity
import com.codewithngoc.instagallery.db.entities.PostMediaEntity
import com.codewithngoc.instagallery.db.entities.UserEntity
import com.codewithngoc.instagallery.db.tables.CommentsTable
import com.codewithngoc.instagallery.db.tables.FiltersTable
import com.codewithngoc.instagallery.db.tables.FollowersTable
import com.codewithngoc.instagallery.db.tables.LikesTable
import com.codewithngoc.instagallery.db.tables.PostMediaTable
import com.codewithngoc.instagallery.db.tables.PostsTable
import com.codewithngoc.instagallery.db.tables.PostVisibility
import com.codewithngoc.instagallery.db.tables.UsersTable
import com.codewithngoc.instagallery.db.utils.dbQuery
import com.codewithngoc.instagallery.domain.models.MediaItem
import com.codewithngoc.instagallery.domain.models.MediaResponse
import com.codewithngoc.instagallery.domain.models.Post
import com.codewithngoc.instagallery.domain.models.User
import com.codewithngoc.instagallery.domain.repos.PostRepository
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

class PostRepositoryImpl : PostRepository {

    override suspend fun createPost(
        userId: Int,
        caption: String?,
        visibility: PostVisibility,
        location: String?
    ): Int = dbQuery {
        val now = Instant.now()

        PostEntity.new {
            this.userId = EntityID(userId, PostsTable)
            this.caption = caption
            this.visibility = visibility
            this.location = location
            this.likeCount = 0
            this.commentCount = 0
            this.createdAt = now
            this.updatedAt = now
        }.id.value
    }

    override suspend fun addPostMedia(postId: Int, mediaItems: List<MediaItem>) = dbQuery {
        mediaItems.forEach { item ->
            PostMediaEntity.new {
                this.postId = PostEntity.findById(postId)
                    ?: throw IllegalArgumentException("Post with ID $postId not found")
                this.mediaFileUrl = item.mediaFileUrl
                this.thumbnailUrl = item.thumbnailUrl
                this.mediaType = item.mediaType
                this.position = item.position
                this.filterId = item.filterId?.let { EntityID(it, FiltersTable) }
                this.metadata = item.metadata
            }
        }
    }

    override suspend fun getPostById(postId: Int): Post? = dbQuery {
        PostEntity.findById(postId)?.let {
            Post(
                postId = it.id.value,
                userId = it.userId.value,
                caption = it.caption,
                location = it.location,
                visibility = it.visibility,
                likeCount = it.likeCount,
                commentCount = it.commentCount,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }
    }


    override suspend fun updatePost(
        postId: Int,
        caption: String?,
        visibility: PostVisibility?,
        location: String?
    ): Post? = dbQuery {
        val postEntity = PostEntity.findById(postId) ?: return@dbQuery null
        postEntity.updateFrom(
            com.codewithngoc.instagallery.domain.models.UpdatePostRequest(
                caption = caption,
                visibility = visibility,
                location = location
            )
        )
        Post(
            postId = postEntity.id.value,
            userId = postEntity.userId.value,
            caption = postEntity.caption,
            location = postEntity.location,
            visibility = postEntity.visibility,
            likeCount = postEntity.likeCount,
            commentCount = postEntity.commentCount,
            createdAt = postEntity.createdAt,
            updatedAt = postEntity.updatedAt
        )
    }

    override suspend fun deletePost(postId: Int): Boolean = dbQuery {
        val deletedRows = PostsTable.deleteWhere { PostsTable.id eq postId }
        deletedRows > 0
    }

    override suspend fun getPostAuthorId(postId: Int): Int? = dbQuery {
        PostsTable.select { PostsTable.id eq postId }.singleOrNull()?.get(PostsTable.userId)?.value
    }

    override suspend fun getPostVisibility(postId: Int): PostVisibility? = dbQuery {
        PostsTable.select { PostsTable.id eq postId }.singleOrNull()?.get(PostsTable.visibility)
    }

    override suspend fun getFeedPosts(userId: Int, limit: Int, offset: Int): List<Post> = dbQuery {
        val followedUsersIds = FollowersTable
            .select { FollowersTable.followerId eq userId }
            .map { it[FollowersTable.followingId].value }

        PostsTable
            .select { (PostsTable.userId inList followedUsersIds) or (PostsTable.userId eq userId) }
            .orderBy(PostsTable.createdAt to SortOrder.DESC)
            .limit(limit, offset.toLong())
            .map { postRow ->
                Post(
                    postId = postRow[PostsTable.id].value,
                    userId = postRow[PostsTable.userId].value,
                    caption = postRow[PostsTable.caption],
                    location = postRow[PostsTable.location],
                    visibility = postRow[PostsTable.visibility],
                    likeCount = postRow[PostsTable.likeCount],
                    commentCount = postRow[PostsTable.commentCount],
                    createdAt = postRow[PostsTable.createdAt],
                    updatedAt = postRow[PostsTable.updatedAt]
                )
            }
    }

    override suspend fun getExplorePosts(limit: Int, offset: Int): List<Post> = dbQuery {
        PostsTable
            .select { PostsTable.visibility eq PostVisibility.PUBLIC }
            .orderBy(PostsTable.createdAt to SortOrder.DESC)
            .limit(limit, offset.toLong())
            .map { postRow ->
                Post(
                    postId = postRow[PostsTable.id].value,
                    userId = postRow[PostsTable.userId].value,
                    caption = postRow[PostsTable.caption],
                    location = postRow[PostsTable.location],
                    visibility = postRow[PostsTable.visibility],
                    likeCount = postRow[PostsTable.likeCount],
                    commentCount = postRow[PostsTable.commentCount],
                    createdAt = postRow[PostsTable.createdAt],
                    updatedAt = postRow[PostsTable.updatedAt]
                )
            }
    }

    override suspend fun getMediaForPost(postId: Int): List<MediaResponse> = dbQuery {
        PostMediaEntity
            .find { PostMediaTable.postId eq postId }
            .sortedBy { it.position }
            .map { it.toMediaResponse() }
    }

    override suspend fun getPostLikeCount(postId: Int): Int = dbQuery {
        LikesTable.select { LikesTable.postId eq postId }.count().toInt()
    }

    override suspend fun getPostCommentCount(postId: Int): Int = dbQuery {
        CommentsTable.select { CommentsTable.postId eq postId }.count().toInt()
    }

    override suspend fun deleteAllPosts(): Boolean = dbQuery {
        val deleted = PostEntity.all().count()
        PostEntity.all().forEach { it.delete() }
        deleted > 0
    }

    override suspend fun getAllPosts(): List<Post> = dbQuery {
        PostEntity
            .all()
            .orderBy(PostsTable.createdAt to SortOrder.DESC) // Sắp xếp mới nhất trước
            .map { postEntity ->
                Post(
                    postId = postEntity.id.value,
                    userId = postEntity.userId.value,
                    caption = postEntity.caption,
                    location = postEntity.location,
                    visibility = postEntity.visibility,
                    likeCount = postEntity.likeCount,
                    commentCount = postEntity.commentCount,
                    createdAt = postEntity.createdAt,
                    updatedAt = postEntity.updatedAt
                )
            }
    }

    override suspend fun deleteMediaForPost(postId: Int) {
        dbQuery {
            PostMediaEntity.find { PostMediaTable.postId eq postId }
                .forEach { it.delete() }
        }
    }

    override suspend fun addMediaToPost(postId: Int, mediaItem: MediaItem) {
        dbQuery {
            val post = PostEntity.findById(postId)
                ?: throw IllegalArgumentException("Post not found")

            PostMediaEntity.new {
                this.postId = post // ✅ Gán PostEntity thay vì Int
                this.mediaFileUrl = mediaItem.mediaFileUrl
                this.thumbnailUrl = mediaItem.thumbnailUrl
                this.mediaType = mediaItem.mediaType
                this.position = mediaItem.position
                this.filterId = mediaItem.filterId?.let { EntityID(it, FiltersTable) } // nếu có FiltersTable
                this.metadata = mediaItem.metadata
            }
        }
    }

    override suspend fun addComment(
        postId: Int,
        userId: Int,
        content: String,
        parentCommentId: Int?
    ): Int = dbQuery {
        val now = Instant.now()
        val newComment = CommentEntity.new {
            this.postId = EntityID(postId, PostsTable)
            this.userId = EntityID(userId, UsersTable)
            this.content = content
            this.parentCommentId = parentCommentId?.let { EntityID(it, CommentsTable) }
            this.createdAt = now
            this.updatedAt = now
        }
        newComment.id.value
    }

    override suspend fun getCommentsForPost(
        postId: Int,
        page: Int,
        size: Int
    ): List<CommentEntity> = dbQuery {
        CommentsTable
            .select { CommentsTable.postId eq postId }
            .orderBy(CommentsTable.createdAt to SortOrder.DESC)
            .limit(size, (page * size).toLong())
            .map { CommentEntity.wrapRow(it) }
    }

    override suspend fun getCommentById(commentId: Int): CommentEntity? = dbQuery {
        CommentEntity.findById(commentId)
    }

    override suspend fun incrementCommentCount(postId: Int): Unit = dbQuery {
        PostsTable.update({ PostsTable.id eq postId }) {
            with(SqlExpressionBuilder) {
                it.update(commentCount, commentCount + 1)
            }
        }
    }

    override suspend fun getUserPosts(userId: Int, page: Int, size: Int): List<Post> = dbQuery {
        val offset = (page * size).toLong()
        PostsTable
            .select { PostsTable.userId eq userId }
            .orderBy(PostsTable.createdAt to SortOrder.DESC)
            .limit(size, offset)
            .map { postRow ->
                val postId = postRow[PostsTable.id].value
                val mediaItems = PostMediaEntity.find { PostMediaTable.postId eq postId }.map {
                    MediaResponse(
                        mediaId = it.id.value,
                        mediaFileUrl = it.mediaFileUrl,
                        thumbnailUrl = it.thumbnailUrl,
                        mediaType = it.mediaType,
                        position = it.position,
                        filterId = it.filterId?.value,
                        metadata = it.metadata
                    )
                }
                Post(
                    postId = postId,
                    userId = postRow[PostsTable.userId].value,
                    caption = postRow[PostsTable.caption],
                    visibility = postRow[PostsTable.visibility],
                    location = postRow[PostsTable.location],
                    likeCount = postRow[PostsTable.likeCount],
                    commentCount = postRow[PostsTable.commentCount],
                    createdAt = postRow[PostsTable.createdAt],
                    updatedAt = postRow[PostsTable.updatedAt]
                )
            }
    }
}


