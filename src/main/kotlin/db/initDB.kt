package com.codewithngoc.instagallery.db

import com.codewithngoc.instagallery.db.tables.ActivityLogsTable
import com.codewithngoc.instagallery.db.tables.BookingsTable
import com.codewithngoc.instagallery.db.tables.CommentsTable
import com.codewithngoc.instagallery.db.tables.FiltersTable
import com.codewithngoc.instagallery.db.tables.FollowersTable
import com.codewithngoc.instagallery.db.tables.LikesTable
import com.codewithngoc.instagallery.db.tables.MediaTagsTable
import com.codewithngoc.instagallery.db.tables.MessagesTable
import com.codewithngoc.instagallery.db.tables.NotificationsTable
import com.codewithngoc.instagallery.db.tables.PostMediaTable
import com.codewithngoc.instagallery.db.tables.PostMediaTagsTable
import com.codewithngoc.instagallery.db.tables.PostsTable
import com.codewithngoc.instagallery.db.tables.RatingsTable
import com.codewithngoc.instagallery.db.tables.ReportsTable
import com.codewithngoc.instagallery.db.tables.SavedPostsTable
import com.codewithngoc.instagallery.db.tables.SearchHistoriesTable
import com.codewithngoc.instagallery.db.tables.UserSessionsTable
import com.codewithngoc.instagallery.db.tables.UsersTable
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.initDB() {

    // Lấy thông tin cấu hình từ application.conf
    val dbConfig = environment.config.config("database")
    val driver = dbConfig.property("driver").getString()
    val url = dbConfig.property("url").getString()
    val user = dbConfig.property("user").getString()

    // Mật khẩu được lấy từ biến môi trường để bảo mật tốt hơn
    val password = System.getenv(
        "instagallery_database_password"
    )?: throw RuntimeException("Environment variable INSTAGALLERY_DB_PASSWORD not set")


    // Kết nối và tạo bảng
    val db = Database.connect(
        url = url,
        driver = driver,
        user = user,
        password = password
    )

    transaction(db) {
        SchemaUtils.create(
            // Bảng User
            UsersTable,
            // Bảng Post
            PostsTable, FiltersTable, PostMediaTable,
            // Bảng Tương tác
            FollowersTable, LikesTable, CommentsTable, SavedPostsTable,
            // Bảng Booking
            BookingsTable, RatingsTable,
            // Bảng Nhắn tin
            NotificationsTable, MessagesTable,
            // Bảng Bổ sung
            UserSessionsTable, MediaTagsTable, PostMediaTagsTable,
            ActivityLogsTable, ReportsTable, SearchHistoriesTable
        )
    }
}