package com.example.mdmobile.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val position: Int = 0, // For future use: position in file (line number)
    val createdAt: Date = Date(),
    val lastAccessed: Date = Date(),
    val note: String? = null
)