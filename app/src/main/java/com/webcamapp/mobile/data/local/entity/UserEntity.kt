package com.webcamapp.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.webcamapp.mobile.data.model.User
import com.webcamapp.mobile.data.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val role: UserRole,
    val createdAt: Long,
    val lastLoginAt: Long
) {
    fun toUser(): User = User(
        id = id,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        role = role,
        createdAt = createdAt,
        lastLoginAt = lastLoginAt
    )

    companion object {
        fun fromUser(user: User): UserEntity = UserEntity(
            id = user.id,
            email = user.email,
            displayName = user.displayName,
            photoUrl = user.photoUrl,
            role = user.role,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt
        )
    }
}