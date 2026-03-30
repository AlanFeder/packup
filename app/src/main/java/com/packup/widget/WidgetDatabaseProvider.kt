package com.packup.widget

import android.content.Context
import androidx.room.Room
import com.packup.data.local.PackUpDatabase

internal object WidgetDatabaseProvider {

    @Volatile
    private var instance: PackUpDatabase? = null

    @Suppress("DEPRECATION")
    fun database(context: Context): PackUpDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PackUpDatabase::class.java,
                "packup.db"
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
}
