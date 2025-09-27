package com.codewithngoc.instagallery.data.repos

import com.codewithngoc.instagallery.db.tables.LikesTable
import com.codewithngoc.instagallery.db.tables.PostsTable
import com.codewithngoc.instagallery.db.tables.UsersTable
import com.codewithngoc.instagallery.db.utils.dbQuery
import com.codewithngoc.instagallery.domain.models.LikeResponse
import com.codewithngoc.instagallery.domain.models.PagedResponse
import com.codewithngoc.instagallery.domain.repos.LikeRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class LikeRepositoryImpl : LikeRepository {
    override suspend fun likePost(userId: Int, postId: Int): Boolean = dbQuery {
        val exists = LikesTable.select {
            (LikesTable.userId eq userId) and (LikesTable.postId eq postId)
        }.count() > 0

        if (!exists) {
            LikesTable.insert {
                it[LikesTable.userId] = userId
                it[LikesTable.postId] = postId
            }
            PostsTable.update({ PostsTable.id eq postId }) {
                with(SqlExpressionBuilder) { it.update(PostsTable.likeCount, PostsTable.likeCount + 1) }
            }
            true
        } else false
    }

    override suspend fun unlikePost(userId: Int, postId: Int): Boolean = dbQuery {
        val deleted = LikesTable.deleteWhere {
            (LikesTable.userId eq userId) and (LikesTable.postId eq postId)
        }
        if (deleted > 0) {
            PostsTable.update({ PostsTable.id eq postId }) {
                with(SqlExpressionBuilder) { it.update(PostsTable.likeCount, PostsTable.likeCount - 1) }
            }
            true
        } else false
    }

    override suspend fun hasUserLikedPost(userId: Int, postId: Int): Boolean = dbQuery {
        LikesTable.select {
            (LikesTable.userId eq userId) and (LikesTable.postId eq postId)
        }.count() > 0
    }

    override suspend fun getLikesForPost(postId: Int, page: Int, size: Int): Pair<List<LikeResponse>, Int> = dbQuery {
        val offset = ((page - 1) * size).toLong()  // FIX offset

        val totalCount = LikesTable.select { LikesTable.postId eq postId }.count().toInt()

        val likes = (LikesTable innerJoin UsersTable)
            .slice(
                UsersTable.id,
                UsersTable.username,
                UsersTable.fullName,
                UsersTable.profilePictureUrl
            )
            .select { LikesTable.postId eq postId }
            .limit(size, offset = offset)
            .map {
                LikeResponse(
                    userId = it[UsersTable.id].value,
                    username = it[UsersTable.username],
                    fullName = it[UsersTable.fullName],
                    profilePictureUrl = it[UsersTable.profilePictureUrl]
                )
            }
        likes to totalCount
    }

    override suspend fun countLikes(postId: Int): Int = dbQuery {
        LikesTable.select { LikesTable.postId eq postId }.count().toInt()
    }
}
