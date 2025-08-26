package com.codewithngoc.instagallery.db.entities
import com.codewithngoc.instagallery.db.tables.UserSessionsTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserSessionEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserSessionEntity>(UserSessionsTable)

    var userId by UserEntity referencedOn UserSessionsTable.userId
    var deviceInfo by UserSessionsTable.deviceInfo
    var ipAddress by UserSessionsTable.ipAddress
    var refreshToken by UserSessionsTable.refreshToken
    var createdAt by UserSessionsTable.createdAt
    var expiredAt by UserSessionsTable.expiredAt
}
