package com.webcamapp.mobile.di

import com.webcamapp.mobile.data.local.UserPreferences
import com.webcamapp.mobile.data.local.dao.DeviceDao
import com.webcamapp.mobile.data.local.dao.MotionEventDao
import com.webcamapp.mobile.data.local.dao.UserDao
import com.webcamapp.mobile.data.repository.UserRepository
import com.webcamapp.mobile.data.repository.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        userPreferences: UserPreferences
    ): UserRepository {
        return UserRepositoryImpl(userDao, userPreferences)
    }
}