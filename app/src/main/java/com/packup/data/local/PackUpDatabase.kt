package com.packup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.packup.data.local.dao.CategoryDao
import com.packup.data.local.dao.FamilyMemberDao
import com.packup.data.local.dao.MorningItemDao
import com.packup.data.local.dao.PackingItemDao
import com.packup.data.local.entity.CategoryEntity
import com.packup.data.local.entity.FamilyMemberEntity
import com.packup.data.local.entity.MorningItemEntity
import com.packup.data.local.entity.PackingItemEntity

@Database(
    entities = [
        FamilyMemberEntity::class,
        PackingItemEntity::class,
        MorningItemEntity::class,
        CategoryEntity::class,
    ],
    version = 2,
    exportSchema = false
)
abstract class PackUpDatabase : RoomDatabase() {
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun packingItemDao(): PackingItemDao
    abstract fun morningItemDao(): MorningItemDao
    abstract fun categoryDao(): CategoryDao
}
