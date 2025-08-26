package com.codewithngoc.instagallery.db.entities

import com.codewithngoc.instagallery.db.tables.CommentsTable
import com.codewithngoc.instagallery.domain.models.AuthorInfoResponse
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CommentEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CommentEntity>(CommentsTable)

    var postId by CommentsTable.postId
    var userId by CommentsTable.userId
    var content by CommentsTable.content
    var parentCommentId by CommentsTable.parentCommentId
    var createdAt by CommentsTable.createdAt
    var updatedAt by CommentsTable.updatedAt

    val user by UserEntity referencedOn CommentsTable.userId

//    fun toCommentResponse(authorInfo: AuthorInfoResponse): CommentResponse {
//        return CommentResponse(
//            commentId = id.value,
//            postId = postId.value,
//            author = authorInfo,
//            content = content,
//            parentCommentId = parentCommentId?.value,
//            createdAt = createdAt.toString()
//        )
//    }
}