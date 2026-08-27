package com.example.blankapp.di

import com.example.blankapp.data.AuthRepository
import com.example.blankapp.data.SupabaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provide AuthRepository as a singleton.
     */
    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepository
    }

    /**
     * Provide SupabaseRepository as a singleton.
     */
    @Provides
    @Singleton
    fun provideSupabaseRepository(): SupabaseRepository {
        return SupabaseRepository
    }
}
