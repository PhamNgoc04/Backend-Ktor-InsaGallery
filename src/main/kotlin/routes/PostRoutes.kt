import com.codewithngoc.instagallery.domain.models.CreatePostRequest
import com.codewithngoc.instagallery.domain.services.PostService
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.postRoutes(postService: PostService) {
    routing {
        authenticate("auth-jwt") {
            route("/api/posts") {
                post {
                    val userId = call.principal<UserIdPrincipal>()?.name?.toIntOrNull()
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid JWT token"))
                        return@post
                    }

                    val request = call.receive<CreatePostRequest>()
                    val createdPost = postService.createPost(userId, request)
                    call.respond(HttpStatusCode.Created, mapOf(
                        "message" to "Post created successfully",
                        "post" to createdPost
                    ))
                }

                get("/feed") {
                    val userId = call.principal<UserIdPrincipal>()?.name?.toIntOrNull()
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid JWT token"))
                        return@get
                    }
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                    val posts = postService.getNewsFeed(userId, page, size)
                    call.respond(posts)
                }

                get("/explore") {
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                    val posts = postService.getExplorePosts(page, size)
                    call.respond(posts)
                }

                route("/{id}") {
                    get {
                        val postId = call.parameters["id"]?.toIntOrNull()
                        val viewerId = call.principal<UserIdPrincipal>()?.name?.toIntOrNull()

                        if (postId == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid post id")
                            return@get
                        }

                        val post = postService.getPostDetail(postId, viewerId)
                        if (post == null) {
                            call.respond(HttpStatusCode.NotFound)
                        } else {
                            call.respond(post)
                        }
                    }

                    put {
                        val userId = call.principal<UserIdPrincipal>()?.name?.toIntOrNull()
                        val postId = call.parameters["id"]?.toIntOrNull()

                        if (userId == null || postId == null) {
                            call.respond(HttpStatusCode.BadRequest)
                            return@put
                        }

                        val request = call.receive<CreatePostRequest>()
                        val updatedPost = postService.updatePost(userId, postId, request)
                        if (updatedPost == null) {
                            call.respond(HttpStatusCode.Forbidden)
                        } else {
                            call.respond(updatedPost)
                        }
                    }

                    delete {
                        val userId = call.principal<UserIdPrincipal>()?.name?.toIntOrNull()
                        val postId = call.parameters["id"]?.toIntOrNull()

                        if (userId == null || postId == null) {
                            call.respond(HttpStatusCode.BadRequest)
                            return@delete
                        }

                        val deleted = postService.deletePost(userId, postId)
                        if (deleted) {
                            call.respond(HttpStatusCode.NoContent)
                        } else {
                            call.respond(HttpStatusCode.Forbidden)
                        }
                    }
                }
            }
        }
    }
}
