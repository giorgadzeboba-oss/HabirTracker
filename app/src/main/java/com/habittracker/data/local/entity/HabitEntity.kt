package com.habittracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val color: String,
    val icon: String,
    val reminderTime: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)