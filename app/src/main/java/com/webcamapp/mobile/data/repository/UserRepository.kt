package com.webcamapp.mobile.data.repository

import com.webcamapp.mobile.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getCurrentUser(): User?
    suspend fun getUserById(userId: String): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun saveUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(user: User)
    fun getCurrentUserFlow(): Flow<User?>
}

class UserRepositoryImpl(
    private val userDao: com.webcamapp.mobile.data.local.dao.UserDao,
    private val userPreferences: com.webcamapp.mobile.data.local.UserPreferences
) : UserRepository {

    override suspend fun getCurrentUser(): User? {
        val currentUserId = userPreferences.getCurrentUserId()
        return currentUserId?.let { userDao.getUserById(it)?.toUser() }
    }

    override suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)?.toUser()
    }

    override suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toUser()
    }

    override suspend fun saveUser(user: User) {
        userDao.insertUser(com.webcamapp.mobile.data.local.entity.UserEntity.fromUser(user))
        userPreferences.setCurrentUserId(user.id)
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(com.webcamapp.mobile.data.local.entity.UserEntity.fromUser(user))
    }

    override suspend fun deleteUser(user: User) {
        userDao.deleteUser(com.webcamapp.mobile.data.local.entity.UserEntity.fromUser(user))
        if (userPreferences.getCurrentUserId() == user.id) {
            userPreferences.clearCurrentUserId()
        }
    }

    override fun getCurrentUserFlow(): Flow<User?> {
        return kotlinx.coroutines.flow.flow {
            val currentUserId = userPreferences.getCurrentUserId()
            if (currentUserId != null) {
                emit(userDao.getUserById(currentUserId)?.toUser())
            } else {
                emit(null)
            }
        }
    }
}