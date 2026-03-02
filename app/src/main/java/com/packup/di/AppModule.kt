package com.packup.di

import android.content.Context
import androidx.room.Room
import com.packup.data.local.PackUpDatabase
import com.packup.data.local.dao.CategoryDao
import com.packup.data.local.dao.FamilyMemberDao
import com.packup.data.local.dao.MorningItemDao
import com.packup.data.local.dao.PackingItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Suppress("DEPRECATION")
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PackUpDatabase =
        Room.databaseBuilder(context, PackUpDatabase::class.java, "packup.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideFamilyMemberDao(db: PackUpDatabase): FamilyMemberDao = db.familyMemberDao()
    @Provides fun providePackingItemDao(db: PackUpDatabase): PackingItemDao = db.packingItemDao()
    @Provides fun provideMorningItemDao(db: PackUpDatabase): MorningItemDao = db.morningItemDao()
    @Provides fun provideCategoryDao(db: PackUpDatabase): CategoryDao = db.categoryDao()
}
