package com.habittracker.domain.model

data class Habit(
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: HabitCategory,
    val color: String,
    val icon: String,
    val reminderTime: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val isCompletedToday: Boolean = false,
    val currentStreak: Int = 0,
    val totalCompletions: Int = 0,
    val completionRate: Float = 0f
)

enum class HabitCategory(val displayName: String, val emoji: String) {
    HEALTH("ჯანმრთელობა", "💪"),
    MIND("გონება", "🧠"),
    PRODUCTIVITY("პროდუქტიულობა", "⚡"),
    SOCIAL("სოციალური", "👥"),
    CREATIVITY("კრეატიულობა", "🎨"),
    OTHER("სხვა", "✨")
}