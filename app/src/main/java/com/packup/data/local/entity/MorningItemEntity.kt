package com.packup.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MorningItemStatus {
    TODO, DONE
}

@Entity(tableName = "morning_items")
data class MorningItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String = "General",
    val status: MorningItemStatus = MorningItemStatus.TODO,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedByDeviceId: String = ""
)
