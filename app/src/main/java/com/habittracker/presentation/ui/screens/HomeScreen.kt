package com.habittracker.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.domain.model.HabitCategory
import com.habittracker.presentation.ui.components.HabitCard
import com.habittracker.presentation.ui.components.ProgressSummaryCard
import com.habittracker.presentation.viewmodel.HabitViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HabitViewModel,
    onAddHabit: () -> Unit,
    onEditHabit: (Int) -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToCalendar: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredHabits by viewModel.filteredHabits.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM, yyyy", Locale("ka")))

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "🎯 Habit Tracker",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("მთავარი") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("სტატისტიკა") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToStats()
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text("ჩვევის დამატება") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onAddHabit()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("🎯 Habit Tracker", fontWeight = FontWeight.Bold)
                            Text(
                                text = today,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "მენიუ")
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToStats) {
                            Icon(Icons.Default.BarChart, contentDescription = "სტატისტიკა")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddHabit) {
                    Icon(Icons.Default.Add, contentDescription = "ჩვევის დამატება")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("ჩვევის ძიება...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "გასუფთავება")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )

                ProgressSummaryCard(
                    completed = viewModel.getTodayCompletionCount(),
                    total = viewModel.getTotalHabitsCount(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory == null,
                            onClick = { viewModel.setCategory(null) },
                            label = { Text("ყველა") }
                        )
                    }
                    items(HabitCategory.values()) { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.setCategory(if (uiState.selectedCategory == category) null else category) },
                            label = { Text("${category.emoji} ${category.displayName}") }
                        )
                    }
                }

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filteredHabits.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌱", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (uiState.habits.isEmpty()) "პირველი ჩვევა დაამატე!" else "არ მოიძებნა",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredHabits, key = { it.id }) { habit ->
                            HabitCard(
                                habit = habit,
                                onToggle = { viewModel.toggleHabitCompletion(habit.id) },
                                onEdit = { onEditHabit(habit.id) },
                                onDelete = { showDeleteDialog = habit.id },
                                onCalendarClick = { onNavigateToCalendar(habit.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { habitId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("ჩვევის წაშლა") },
            text = { Text("დარწმუნებული ხარ, რომ გინდა ჩვევის წაშლა?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHabit(habitId)
                    showDeleteDialog = null
                }) { Text("წაშლა", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("გაუქმება") }
            }
        )
    }
}