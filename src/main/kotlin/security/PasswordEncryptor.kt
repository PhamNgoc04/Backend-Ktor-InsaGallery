package com.codewithngoc.instagallery.security

import org.mindrot.jbcrypt.BCrypt

object PasswordEncryptor {
    fun hash(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12))
    }

    fun verify(password: String, hash: String): Boolean {
        return try {
            BCrypt.checkpw(password, hash)
        } catch (e: Exception) {
            false
        }
    }
}