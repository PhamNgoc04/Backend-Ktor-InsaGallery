package com.codewithngoc.instagallery.db.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

// Bảng USERS
object UsersTable : IntIdTable("users") {
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val fullName = varchar("full_name", 100)
    val profilePictureUrl = varchar("profile_picture_url", 255).nullable()
    val bio = text("bio").nullable()
    val userType = enumerationByName("user_type", 12, UserType::class)
    val role = enumerationByName("role", 10, Role::class).default(Role.USER)
    val location = varchar("location", 255).nullable()
    val isVerified = bool("is_verified").default(false)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

enum class UserType {
    PHOTOGRAPHER, CLIENT, ENTHUSIAST
}

enum class Role {
    USER, ADMIN
}

// Bảng POSTS
object PostsTable : IntIdTable("posts") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val caption = text("caption").nullable()
    val location = varchar("location", 255).nullable()
    val visibility = enumerationByName("visibility", 30, PostVisibility::class).default(PostVisibility.PUBLIC)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() } // ✅ Fix
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }
}

enum class PostVisibility {
    PUBLIC, PRIVATE, FRIENDS_ONLY
}

// Bảng POST_MEDIA
object PostMediaTable : IntIdTable("post_media") {
    val postId = reference("post_id", PostsTable, onDelete = ReferenceOption.CASCADE)
    val mediaFileUrl = varchar("media_file_url", 255)
    val thumbnailUrl = varchar("thumbnail_url", 255).nullable()
    val mediaType = enumerationByName("media_type", 10, MediaType::class).default(MediaType.IMAGE)
    val position = integer("position").default(0)
    val filterId = reference("filter_id", FiltersTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val metadata = text("metadata").nullable()
}

enum class MediaType {
    IMAGE, VIDEO
}

// Bảng FILTERS
object FiltersTable : IntIdTable("filters") {
    val name = varchar("name", 50).uniqueIndex()
    val description = text("description").nullable()
}

// Bảng FOLLOWERS (nhiều-nhiều -> giữ Table)
object FollowersTable : Table("followers") {
    val followerId = reference("follower_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val followingId = reference("following_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(followerId, followingId)
}

// Bảng LIKES (nhiều-nhiều -> giữ Table)
object LikesTable : IntIdTable("likes") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val postId = reference("post_id", PostsTable, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at")

//    override val primaryKey = PrimaryKey(userId, postId)
}

// Bảng COMMENTS
object CommentsTable : IntIdTable("comments") {
    val postId = reference("post_id", PostsTable, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val content = text("content")
    val parentCommentId = reference("parent_comment_id", CommentsTable).nullable()
    val createdAt = timestamp("created_at")
}

// Bảng SAVED_POSTS (nhiều-nhiều -> giữ Table)
object SavedPostsTable : IntIdTable("saved_posts") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val postId = reference("post_id", PostsTable, onDelete = ReferenceOption.CASCADE)
    val savedAt = timestamp("saved_at")

//    override val primaryKey = PrimaryKey(userId, postId)
}

// Bảng BOOKINGS
object BookingsTable : IntIdTable("bookings") {
    val clientId = reference("client_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val photographerId = reference("photographer_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val bookingDate = datetime("booking_date")
    val locationBooking = varchar("location_booking", 255).nullable()
    val details = text("details").nullable()
    val price = decimal("price", 10, 2).nullable()
    val status = enumerationByName("status", 20, BookingStatus::class).default(BookingStatus.PENDING)
    val createdAt = timestamp("created_at")
}

enum class BookingStatus {
    PENDING, CONFIRMED, COMPLETED, CANCELLED
}

// Bảng RATINGS
object RatingsTable : IntIdTable("ratings") {
    val bookingId = reference("booking_id", BookingsTable, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val raterId = reference("rater_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val rateeId = reference("ratee_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val ratingValue = short("rating_value")
    val comment = text("comment").nullable()
    val createdAt = timestamp("created_at")
}

// Bảng NOTIFICATIONS
object NotificationsTable : IntIdTable("notifications") {
    val recipientId = reference("recipient_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val senderId = reference("sender_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val type = enumerationByName("type", 20, NotificationType::class)
    val targetId = integer("target_id")
    val isRead = bool("is_read").default(false)
    val createdAt = timestamp("created_at")
}

enum class NotificationType {
    NEW_LIKE, NEW_COMMENT, NEW_FOLLOWER, BOOKING_REQUEST, BOOKING_CONFIRMED, NEW_MESSAGE
}

// Bảng MESSAGES
object MessagesTable : IntIdTable("messages") {
    val senderId = reference("sender_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val receiverId = reference("receiver_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val content = text("content").nullable()
    val messageType = enumerationByName("message_type", 10, MessageType::class).default(MessageType.TEXT)
    val mediaUrl = varchar("media_url", 255).nullable()
    val isRead = bool("is_read").default(false)
    val createdAt = timestamp("created_at")
}

enum class MessageType {
    TEXT, IMAGE, VIDEO
}

// Bảng USER_SESSIONS
object UserSessionsTable : IntIdTable("user_sessions") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val deviceInfo = varchar("device_info", 255).nullable()
    val ipAddress = varchar("ip_address", 45).nullable()
    val refreshToken = varchar("refresh_token", 255).uniqueIndex()
    val createdAt = timestamp("created_at")
    val expiredAt = timestamp("expired_at")
}

// Bảng MEDIA_TAGS
object MediaTagsTable : IntIdTable("media_tags") {
    val name = varchar("name", 50).uniqueIndex()
    val description = text("description").nullable()
    val createdAt = timestamp("created_at")
}

// Bảng trung gian POST_MEDIA_TAGS (nhiều-nhiều -> giữ Table)
object PostMediaTagsTable : Table("post_media_tags") {
    val mediaId = reference("media_id", PostMediaTable, onDelete = ReferenceOption.CASCADE)
    val tagId = reference("tag_id", MediaTagsTable, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(mediaId, tagId)
}

// Bảng ACTIVITY_LOGS
object ActivityLogsTable : IntIdTable("activity_logs") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val action = varchar("action", 255)
    val targetType = enumerationByName("target_type", 20, LogTargetType::class)
    val targetId = integer("target_id").nullable()
    val metadata = text("metadata").nullable()
    val createdAt = timestamp("created_at")
}

enum class LogTargetType {
    POST, USER, COMMENT, BOOKING
}

// Bảng REPORTS
object ReportsTable : IntIdTable("reports") {
    val reporterId = reference("reporter_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val targetType = enumerationByName("target_type", 20, ReportTargetType::class)
    val targetId = integer("target_id")
    val reason = text("reason")
    val status = enumerationByName("status", 20, ReportStatus::class).default(ReportStatus.PENDING)
    val createdAt = timestamp("created_at")
}

enum class ReportTargetType {
    POST, COMMENT, USER, BOOKING
}

enum class ReportStatus {
    PENDING, REVIEWED, DISMISSED
}

// Bảng SEARCH_HISTORIES
object SearchHistoriesTable : IntIdTable("search_histories") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val queryText = varchar("query_text", 255)
    val searchedAt = timestamp("searched_at")
}
