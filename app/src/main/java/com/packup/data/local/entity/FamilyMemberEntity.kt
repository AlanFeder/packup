package com.packup.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatar: String,
    val iconKey: String = "",
    val photoUri: String = "",
    val sortOrder: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedByDeviceId: String = ""
)
