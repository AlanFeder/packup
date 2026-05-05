package com.packup.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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

private val Context.dataStore by preferencesDataStore(name = "device_preferences")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Suppress("DEPRECATION")
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PackUpDatabase =
        Room.databaseBuilder(context, PackUpDatabase::class.java, "packup.db")
            .fallbackToDestructiveMigration()
            .enableMultiInstanceInvalidation()
            .build()

    @Provides fun provideFamilyMemberDao(db: PackUpDatabase): FamilyMemberDao = db.familyMemberDao()
    @Provides fun providePackingItemDao(db: PackUpDatabase): PackingItemDao = db.packingItemDao()
    @Provides fun provideMorningItemDao(db: PackUpDatabase): MorningItemDao = db.morningItemDao()
    @Provides fun provideCategoryDao(db: PackUpDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore
}
