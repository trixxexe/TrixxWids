package com.trixxwids.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widgets")
data class WidgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val gridWidth: Int, // e.g., 2, 4
    val gridHeight: Int, // e.g., 1, 2, 4
    val isGyroEnabled: Boolean,
    val elementsJson: String, // Serialized List of WidgetElement
    val previewImagePath: String // Path to saved thumbnail in internal storage
)