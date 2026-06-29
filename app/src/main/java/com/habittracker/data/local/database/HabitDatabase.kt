package com.habittracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.habittracker.data.local.dao.HabitDao
import com.habittracker.data.local.entity.HabitEntity
import com.habittracker.data.local.entity.HabitLogEntity

@Database(
    entities = [HabitEntity::class, HabitLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        const val DATABASE_NAME = "habit_tracker_db"
    }
}