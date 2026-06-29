package com.habittracker.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.data.local.PreferencesManager
import com.habittracker.data.repository.HabitRepository
import com.habittracker.domain.model.Habit
import com.habittracker.domain.model.HabitCategory
import com.habittracker.worker.HabitReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HabitUiState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: HabitCategory? = null,
    val searchQuery: String = "",
    val isDarkMode: Boolean = false
)

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitUiState(isLoading = true))
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<HabitCategory?>(null)
    private val _searchQuery = MutableStateFlow("")

    val filteredHabits: StateFlow<List<Habit>> = combine(
        repository.getAllHabits(),
        _selectedCategory,
        _searchQuery
    ) { habits, category, query ->
        habits.filter { habit ->
            val matchesCategory = category == null || habit.category == category
            val matchesSearch = query.isEmpty() ||
                    habit.title.contains(query, ignoreCase = true) ||
                    habit.description.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        observeHabits()
        observeSettings()
    }

    private fun observeHabits() {
        viewModelScope.launch {
            repository.getAllHabits().collect { habits ->
                _uiState.update { it.copy(habits = habits, isLoading = false) }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            preferencesManager.darkModeFlow.collect { isDark ->
                _uiState.update { it.copy(isDarkMode = isDark) }
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateDarkMode(enabled)
        }
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            val id = repository.addHabit(habit)
            habit.reminderTime?.let { time ->
                scheduleReminder(id.toInt(), habit.title, time)
            }
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            if (habit.reminderTime != null) {
                scheduleReminder(habit.id, habit.title, habit.reminderTime)
            } else {
                HabitReminderWorker.cancelReminder(context, habit.id)
            }
        }
    }

    fun deleteHabit(habitId: Int) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            HabitReminderWorker.cancelReminder(context, habitId)
        }
    }

    fun toggleHabitCompletion(habitId: Int) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId)
        }
    }

    fun setCategory(category: HabitCategory?) {
        _selectedCategory.value = category
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getLogsForHabit(habitId: Int): Flow<List<String>> {
        return repository.getLogsForHabit(habitId)
    }

    private fun scheduleReminder(habitId: Int, title: String, timeStr: String) {
        try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val reminderTime = LocalTime.parse(timeStr, formatter)
            val now = LocalTime.now()
            var delayMinutes = now.until(reminderTime, ChronoUnit.MINUTES)
            if (delayMinutes < 0) delayMinutes += 24 * 60
            HabitReminderWorker.scheduleReminder(context, habitId, title, delayMinutes)
        } catch (e: Exception) {
            // Invalid time format
        }
    }

    fun getTodayCompletionCount(): Int = _uiState.value.habits.count { it.isCompletedToday }

    fun getTotalHabitsCount(): Int = _uiState.value.habits.size
}