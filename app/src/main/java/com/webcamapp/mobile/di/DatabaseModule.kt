package com.webcamapp.mobile.di

import android.content.Context
import androidx.room.Room
import com.webcamapp.mobile.data.local.AppDatabase
import com.webcamapp.mobile.data.local.dao.DeviceDao
import com.webcamapp.mobile.data.local.dao.MotionEventDao
import com.webcamapp.mobile.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideDeviceDao(database: AppDatabase): DeviceDao {
        return database.deviceDao()
    }

    @Provides
    fun provideMotionEventDao(database: AppDatabase): MotionEventDao {
        return database.motionEventDao()
    }
}