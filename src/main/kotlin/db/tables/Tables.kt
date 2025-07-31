package com.codewithngoc.instagallery.db.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

// ---------- I. USERS & AUTH ----------

object UsersTable : IntIdTable("users") {
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val fullName = varchar("full_name", 100)

    val profilePictureUrl = varchar("profile_picture_url", 255).nullable()
    val bio = text("bio").nullable()
    val website = varchar("website", 255).nullable()
    val gender = varchar("gender", 10).nullable()
    val phoneNumber = varchar("phone_number", 20).nullable()
    val dateOfBirth = date("date_of_birth").nullable()
    val location = varchar("location", 255).nullable()

    val userType = enumerationByName("user_type", 12, UserType::class)
    val role = enumerationByName("role", 10, Role::class).default(Role.USER)
    val isVerified = bool("is_verified").default(false)

    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }
}

enum class UserType { PHOTOGRAPHER, CLIENT, ENTHUSIAST }
enum class Role { USER, ADMIN }

object UserSessionsTable : IntIdTable("user_sessions") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE).index()
    val deviceInfo = varchar("device_info", 255).nullable()
    val ipAddress = varchar("ip_address", 45).nullable()
    val refreshToken = varchar("refresh_token", 255).uniqueIndex()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val expiredAt = timestamp("expired_at")
}

object FollowersTable : Table("followers") {
    val followerId = reference("follower_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val followingId = reference("following_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    override val primaryKey = PrimaryKey(followerId, followingId)

    init { index("idx_following_follower", false, followingId, followerId) }
}

object SearchHistoriesTable : IntIdTable("search_histories") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE).index()
    val queryText = varchar("query_text", 255)
    val searchedAt = timestamp("searched_at").clientDefault { Instant.now() }
}

// ---------- II. POSTS ----------

object PostsTable : IntIdTable("posts") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val caption = text("caption").nullable()
    val location = varchar("location", 255).nullable()
    val visibility = enumerationByName("visibility", 30, PostVisibility::class).default(PostVisibility.PUBLIC)
    val likeCount = integer("like_count").default(0)
    val commentCount = integer("comment_count").default(0)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }

    init { index("idx_post_user_created", false, userId, createdAt) }
}

enum class PostVisibility { PUBLIC, PRIVATE, FRIENDS_ONLY }

object PostMediaTable : IntIdTable("post_media") {
    val postId = reference("post_id", PostsTable, onDelete = ReferenceOption.CASCADE).index()
    val mediaFileUrl = varchar("media_file_url", 255)
    val thumbnailUrl = varchar("thumbnail_url", 255).nullable()
    val mediaType = enumerationByName("media_type", 10, MediaType::class).default(MediaType.IMAGE)
    val position = integer("position").default(0)
    val filterId = reference("filter_id", FiltersTable, onDelete = ReferenceOption.SET_NULL).nullable().index()
    val metadata = text("metadata").nullable()

    init { index(true, postId, position) }
}

enum class MediaType { IMAGE, VIDEO }

object FiltersTable : IntIdTable("filters") {
    val name = varchar("name", 50).uniqueIndex()
    val description = text("description").nullable()
}

object MediaTagsTable : IntIdTable("media_tags") {
    val name = varchar("name", 50).uniqueIndex()
    val description = text("description").nullable()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
}

object PostMediaTagsTable : Table("post_media_tags") {
    val mediaId = reference("media_id", PostMediaTable, onDelete = ReferenceOption.CASCADE).index()
    val tagId = reference("tag_id", MediaTagsTable, onDelete = ReferenceOption.CASCADE).index()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    override val primaryKey = PrimaryKey(mediaId, tagId)
}

// ---------- III. INTERACTIONS ----------

object LikesTable : IntIdTable("likes") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE).index()
    val postId = reference("post_id", PostsTable, onDelete = ReferenceOption.CASCADE).index()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
}

object CommentsTable : IntIdTable("comments") {
    val postId = reference("post_id", PostsTable, onDelete = ReferenceOption.CASCADE).index()
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE).index()
    val content = text("content")
    val parentCommentId = reference("parent_comment_id", CommentsTable).nullable().index()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }

    init { index("idx_comment_post_user", false, postId, userId) }
}

object SavedPostsTable : IntIdTable("saved_posts") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE).index()
    val postId = reference("post_id", PostsTable, onDelete = ReferenceOption.CASCADE).index()
    val savedAt = timestamp("saved_at").clientDefault { Instant.now() }
}

object NotificationsTable : IntIdTable("notifications") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val senderId = reference("sender_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val type = enumerationByName("type", 20, NotificationType::class)
    val targetId = integer("target_id")
    val isRead = bool("is_read").default(false)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }

    init { index("idx_notify_user_created", false, userId, createdAt) }
}

enum class NotificationType {
    NEW_LIKE, NEW_COMMENT, NEW_FOLLOWER, BOOKING_REQUEST, BOOKING_CONFIRMED, NEW_MESSAGE
}

// ---------- IV. MESSAGES ----------

object MessagesTable : IntIdTable("messages") {
    val senderId = reference("sender_id", UsersTable, onDelete = ReferenceOption.CASCADE).index()
    val receiverId = reference("receiver_id", UsersTable, onDelete = ReferenceOption.CASCADE).index()
    val conversationId = integer("conversation_id").nullable().index()
    val content = text("content").nullable()
    val messageType = enumerationByName("message_type", 10, MessageType::class).default(MessageType.TEXT)
    val mediaUrl = varchar("media_url", 255).nullable()
    val isRead = bool("is_read").default(false)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
}

enum class MessageType { TEXT, IMAGE, VIDEO }

// ---------- V. BOOKINGS ----------

object BookingsTable : IntIdTable("bookings") {
    val clientId = reference("client_id", UsersTable, onDelete = ReferenceOption.CASCADE).index()
    val photographerId = reference("photographer_id", UsersTable, onDelete = ReferenceOption.CASCADE).index()
    val bookingDate = datetime("booking_date")
    val locationBooking = varchar("location_booking", 255).nullable()
    val details = text("details").nullable()
    val price = decimal("price", 10, 2).nullable()
    val status = enumerationByName("status", 20, BookingStatus::class).default(BookingStatus.PENDING)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }
}

enum class BookingStatus { PENDING, CONFIRMED, COMPLETED, CANCELLED }

object RatingsTable : IntIdTable("ratings") {
    val bookingId = reference("booking_id", BookingsTable, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val raterId = reference("rater_id", UsersTable, onDelete = ReferenceOption.CASCADE).index()
    val rateeId = reference("ratee_id", UsersTable, onDelete = ReferenceOption.CASCADE).index()
    val ratingValue = short("rating_value")
    val comment = text("comment").nullable()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
}

// ---------- VI. LOGGING & REPORTING ----------

object ActivityLogsTable : IntIdTable("activity_logs") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.SET_NULL).nullable().index()
    val action = varchar("action", 255)
    val targetType = enumerationByName("target_type", 20, LogTargetType::class)
    val targetId = integer("target_id").nullable()
    val metadata = text("metadata").nullable()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
}

enum class LogTargetType { POST, USER, COMMENT, BOOKING }

object ReportsTable : IntIdTable("reports") {
    val reporterId = reference("reporter_id", UsersTable, onDelete = ReferenceOption.CASCADE).index()
    val targetType = enumerationByName("target_type", 20, ReportTargetType::class)
    val targetId = integer("target_id")
    val reason = text("reason")
    val status = enumerationByName("status", 20, ReportStatus::class).default(ReportStatus.PENDING)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
}

enum class ReportTargetType { POST, COMMENT, USER, BOOKING }
enum class ReportStatus { PENDING, REVIEWED, DISMISSED }
