package com.packup.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ItemStatus {
    TODO, DONE, SNOOZED
}

@Entity(
    tableName = "packing_items",
    foreignKeys = [
        ForeignKey(
            entity = FamilyMemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId")]
)
data class PackingItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val status: ItemStatus = ItemStatus.TODO,
    val memberId: String,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedByDeviceId: String = ""
)
