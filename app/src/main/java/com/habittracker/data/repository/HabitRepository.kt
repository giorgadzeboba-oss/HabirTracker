package com.habittracker.data.repository

import com.habittracker.data.local.dao.HabitDao
import com.habittracker.data.local.entity.HabitEntity
import com.habittracker.data.local.entity.HabitLogEntity
import com.habittracker.domain.model.Habit
import com.habittracker.domain.model.HabitCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val dao: HabitDao
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val today get() = LocalDate.now().format(dateFormatter)

    fun getAllHabits(): Flow<List<Habit>> {
        return combine(
            dao.getAllActiveHabits(),
            dao.observeAllLogs()
        ) { entities, logs ->
            entities.map { entity ->
                val habitLogs = logs.filter { it.habitId == entity.id }
                mapToDomainWithLogs(entity, habitLogs)
            }
        }
    }

    suspend fun getHabitById(id: Int): Habit? {
        val entity = dao.getHabitById(id) ?: return null
        val logs = dao.getAllLogDates(id).map { HabitLogEntity(habitId = id, date = it) }
        return mapToDomainWithLogs(entity, logs)
    }

    suspend fun addHabit(habit: Habit): Long {
        return dao.insertHabit(mapToEntity(habit))
    }

    suspend fun updateHabit(habit: Habit) {
        dao.updateHabit(mapToEntity(habit))
    }

    suspend fun deleteHabit(id: Int) {
        dao.softDeleteHabit(id)
    }

    suspend fun toggleHabitCompletion(habitId: Int) {
        val isCompleted = dao.isHabitCompletedOnDate(habitId, today)
        if (isCompleted) {
            dao.deleteLog(habitId, today)
        } else {
            dao.insertLog(HabitLogEntity(habitId = habitId, date = today))
        }
    }

    fun getLogsForHabit(habitId: Int): Flow<List<String>> {
        return dao.getLogsForHabit(habitId).map { logs -> logs.map { it.date } }
    }

    private fun mapToDomainWithLogs(entity: HabitEntity, logs: List<HabitLogEntity>): Habit {
        val logDates = logs.map { it.date }
        val isCompleted = logDates.contains(today)
        val streak = calculateStreakFromDates(logDates)
        val total = logs.size
        val daysSinceCreation = ((System.currentTimeMillis() - entity.createdAt) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
        val rate = (total.toFloat() / daysSinceCreation).coerceIn(0f, 1f)

        return Habit(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            category = try { HabitCategory.valueOf(entity.category) } catch (e: Exception) { HabitCategory.OTHER },
            color = entity.color,
            icon = entity.icon,
            reminderTime = entity.reminderTime,
            createdAt = entity.createdAt,
            isActive = entity.isActive,
            isCompletedToday = isCompleted,
            currentStreak = streak,
            totalCompletions = total,
            completionRate = rate
        )
    }

    private fun calculateStreakFromDates(dates: List<String>): Int {
        if (dates.isEmpty()) return 0

        val sortedDates = dates.map {
            LocalDate.parse(it, dateFormatter)
        }.sortedDescending()

        var currentStreak = 0
        var expectedDate = LocalDate.now()

        for (date in sortedDates) {
            if (date == expectedDate || date == expectedDate.minusDays(1)) {
                if (date == expectedDate || date == expectedDate.minusDays(1)) {
                    currentStreak++
                    expectedDate = date.minusDays(1)
                }
            } else {
                break
            }
        }
        return currentStreak
    }

    private fun mapToEntity(habit: Habit): HabitEntity {
        return HabitEntity(
            id = habit.id,
            title = habit.title,
            description = habit.description,
            category = habit.category.name,
            color = habit.color,
            icon = habit.icon,
            reminderTime = habit.reminderTime,
            createdAt = habit.createdAt,
            isActive = habit.isActive
        )
    }
}