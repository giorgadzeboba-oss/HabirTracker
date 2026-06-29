package com.habittracker.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.domain.model.Habit
import com.habittracker.domain.model.HabitCategory
import com.habittracker.presentation.viewmodel.HabitViewModel

val habitColors = listOf(
    "#4CAF50", "#2196F3", "#F9A825", "#E91E63",
    "#9C27B0", "#FF5722", "#00BCD4", "#607D8B"
)

val habitIcons = listOf(
    "💪", "🧠", "📚", "🏃", "🧘", "💧", "🥗", "😴",
    "✍️", "🎨", "🎵", "🌿", "☀️", "🎯", "💻", "🤝"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    viewModel: HabitViewModel,
    habitId: Int?,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val existingHabit = habitId?.let { id -> uiState.habits.find { it.id == id } }
    val isEditing = existingHabit != null

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(HabitCategory.HEALTH) }
    var selectedColor by remember { mutableStateOf(habitColors[0]) }
    var selectedIcon by remember { mutableStateOf(habitIcons[0]) }
    var reminderTime by remember { mutableStateOf("") }
    var enableReminder by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    LaunchedEffect(existingHabit) {
        existingHabit?.let {
            title = it.title
            description = it.description
            selectedCategory = it.category
            selectedColor = it.color
            selectedIcon = it.icon
            reminderTime = it.reminderTime ?: ""
            enableReminder = it.reminderTime != null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "ჩვევის რედაქტირება" else "ახალი ჩვევა") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "უკან")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("ჩვევის სახელი *") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError,
                supportingText = if (titleError) ({ Text("სახელი სავალდებულოა") }) else null,
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("აღწერა (სურვილისამებრ)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Text("კატეგორია", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(HabitCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedCategory = category },
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.CenterStart) {
                            Text(
                                text = "${category.emoji} ${category.displayName}",
                                modifier = Modifier.padding(horizontal = 12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            Text("ფერი", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                habitColors.forEach { colorHex ->
                    val color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Gray }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(if (selectedColor == colorHex) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                            .clickable { selectedColor = colorHex }
                    )
                }
            }

            Text("ხატი", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                modifier = Modifier.height(100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(habitIcons) { icon ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedIcon == icon) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedIcon = icon },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(icon, fontSize = 20.sp)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("შეხსენება", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Switch(checked = enableReminder, onCheckedChange = { enableReminder = it })
            }
            if (enableReminder) {
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text("დრო (HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("09:00") },
                    singleLine = true
                )
            }

            Button(
                onClick = {
                    if (title.isBlank()) { titleError = true; return@Button }
                    val habit = Habit(
                        id = existingHabit?.id ?: 0,
                        title = title.trim(),
                        description = description.trim(),
                        category = selectedCategory,
                        color = selectedColor,
                        icon = selectedIcon,
                        reminderTime = if (enableReminder && reminderTime.isNotBlank()) reminderTime else null
                    )
                    if (isEditing) viewModel.updateHabit(habit)
                    else viewModel.addHabit(habit)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "განახლება" else "შენახვა")
            }
        }
    }
}
