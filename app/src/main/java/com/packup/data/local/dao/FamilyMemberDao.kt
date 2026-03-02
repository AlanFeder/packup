package com.packup.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.packup.data.local.entity.FamilyMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY sortOrder ASC")
    fun getAllMembers(): Flow<List<FamilyMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: FamilyMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<FamilyMemberEntity>)

    @Query("UPDATE family_members SET name = :name, avatar = :avatar, updatedAt = :now WHERE id = :id")
    suspend fun updateName(id: String, name: String, avatar: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE family_members SET iconKey = :iconKey, updatedAt = :now WHERE id = :id")
    suspend fun updateIcon(id: String, iconKey: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE family_members SET photoUri = :photoUri, updatedAt = :now WHERE id = :id")
    suspend fun updatePhotoUri(id: String, photoUri: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM family_members")
    suspend fun deleteAll()
}
