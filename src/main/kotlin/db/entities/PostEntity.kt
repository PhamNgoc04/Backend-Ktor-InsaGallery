package com.codewithngoc.instagallery.db.entities

import com.codewithngoc.instagallery.db.tables.CommentsTable.clientDefault
import com.codewithngoc.instagallery.db.tables.PostMediaTable
import com.codewithngoc.instagallery.db.tables.PostsTable
import com.codewithngoc.instagallery.db.tables.PostVisibility
import com.codewithngoc.instagallery.domain.models.AuthorInfoResponse
import com.codewithngoc.instagallery.domain.models.MediaResponse
import com.codewithngoc.instagallery.domain.models.PostResponse
import com.codewithngoc.instagallery.domain.models.UpdatePostRequest
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

class PostEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PostEntity>(PostsTable)

    var userId by PostsTable.userId
    var caption by PostsTable.caption
    var location by PostsTable.location
    var visibility by PostsTable.visibility
    var likeCount by PostsTable.likeCount
    var commentCount by PostsTable.commentCount
    var createdAt by PostsTable.createdAt
    var updatedAt by PostsTable.updatedAt

    val author by UserEntity referencedOn PostsTable.userId
    val media by PostMediaEntity referrersOn PostMediaTable.postId

    fun toPostResponse(authorInfo: AuthorInfoResponse, mediaList: List<MediaResponse>): PostResponse {
        return PostResponse(
            postId = id.value,
            author = authorInfo,
            caption = caption,
            location = location,
            visibility = visibility,
            media = mediaList,
            likeCount = likeCount,
            commentCount = commentCount
        )
    }

    // Hàm cập nhật từ request
    fun updateFrom(request: UpdatePostRequest) {
        println("🔧 Updating PostEntity with: $request")

        request.caption?.let {
            println("➡️ Updating caption: $it")
            this.caption = it
        }

        request.visibility?.let {
            println("➡️ Updating visibility: $it")
            this.visibility = it
        }

        request.location?.let {
            println("➡️ Updating location: $it")
            this.location = it
        }

        updatedAt = Instant.now()
        println("✅ updatedAt = $updatedAt")
    }

}
