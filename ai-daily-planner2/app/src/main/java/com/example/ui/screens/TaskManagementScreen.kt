package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Task
import com.example.ui.theme.*
import com.example.ui.viewmodel.PlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagementScreen(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val completedTasksList by viewModel.completedTasks.collectAsState()
    val searchQuery by viewModel.taskSearchQuery.collectAsState()
    val activeCategory by viewModel.taskCategoryFilter.collectAsState()
    val activePriority by viewModel.taskPriorityFilter.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var taskToViewDetail by remember { mutableStateOf<Task?>(null) }

    // Task Create Form state
    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Work") }
    var newPriority by remember { mutableStateOf(1) } // Medium
    var newDuration by remember { mutableStateOf(30) } // Minutes
    var newRecurrence by remember { mutableStateOf("None") }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Task Workspace",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setTaskSearchQuery(it) },
                placeholder = { Text("Search task description or title...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setTaskSearchQuery("") }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentIndigo,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_search_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category filter chips row
            val categories = listOf("All", "Work", "Personal", "Health", "Study", "Social")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = activeCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setTaskCategoryFilter(cat) },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentIndigo,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority filter tags
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Priority:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val priorities = listOf(-1 to "All", 2 to "High", 1 to "Medium", 0 to "Low")
                priorities.forEach { (pCode, pLabel) ->
                    val isSelected = activePriority == pCode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) {
                                    when (pCode) {
                                        2 -> AccentRose
                                        1 -> AccentAmber
                                        0 -> AccentTeal
                                        else -> AccentIndigo
                                    }
                                } else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { viewModel.setTaskPriorityFilter(pCode) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = pLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Task list
            if (tasks.isEmpty()) {
                // Empty states
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Task,
                        contentDescription = "Empty tasks",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tasks found", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Click the '+' button to schedule a new task",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(tasks) { task ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                1.dp,
                                if (task.isCompleted) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { taskToViewDetail = task }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sleek vertical priority strip flush with the left boundary of the card
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(64.dp)
                                        .background(
                                            color = when (task.priority) {
                                                2 -> AccentRose
                                                1 -> AccentAmber
                                                else -> AccentTeal
                                            },
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.toggleTaskCompleted(task) },
                                    colors = CheckboxDefaults.colors(checkedColor = AccentTeal)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = task.title,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (task.description.isNotEmpty()) {
                                        Text(
                                            text = task.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Priority badge
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = when (task.priority) {
                                                        2 -> AccentRose.copy(alpha = 0.15f)
                                                        1 -> AccentAmber.copy(alpha = 0.15f)
                                                        else -> AccentTeal.copy(alpha = 0.15f)
                                                    },
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = when (task.priority) {
                                                    2 -> "High"
                                                    1 -> "Medium"
                                                    else -> "Low"
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (task.priority) {
                                                    2 -> AccentRose
                                                    1 -> AccentAmber
                                                    else -> AccentTeal
                                                }
                                            )
                                        }

                                        // Category badge
                                        Box(
                                            modifier = Modifier
                                                .background(AccentIndigo.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = task.category,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentIndigo
                                            )
                                        }

                                        if (task.estimatedMinutes > 0) {
                                            Text(
                                                text = "⏱️ ${task.estimatedMinutes}m",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Row {
                                    IconButton(onClick = { viewModel.archiveTask(task) }) {
                                        Icon(Icons.Default.Archive, "Archive", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = { viewModel.deleteTask(task) }) {
                                        Icon(Icons.Default.Delete, "Delete", tint = AccentRose.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Task Floating Action Button (FAB)
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = AccentIndigo,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_task_fab")
        ) {
            Icon(Icons.Default.Add, "Add Task")
        }

        // CREATE TASK DIALOG
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create New Task", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Task Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("new_task_title_input")
                        )

                        OutlinedTextField(
                            value = newDescription,
                            onValueChange = { newDescription = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Category Selection
                        Text("Category", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        val catOptions = listOf("Work", "Personal", "Health", "Study", "Social")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(catOptions) { cat ->
                                val isSel = newCategory == cat
                                FilterChip(
                                    selected = isSel,
                                    onClick = { newCategory = cat },
                                    label = { Text(cat) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentIndigo,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Priority Level Selection
                        Text("Priority", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0 to "Low", 1 to "Medium", 2 to "High").forEach { (pCode, pLabel) ->
                                val isSel = newPriority == pCode
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSel) {
                                                when (pCode) {
                                                    2 -> AccentRose
                                                    1 -> AccentAmber
                                                    else -> AccentTeal
                                                }
                                            } else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { newPriority = pCode }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(pLabel, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Duration Slider
                        Text("Estimated Duration: $newDuration mins", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Slider(
                            value = newDuration.toFloat(),
                            onValueChange = { newDuration = it.toInt() },
                            valueRange = 5f..120f,
                            steps = 23,
                            colors = SliderDefaults.colors(
                                activeTrackColor = AccentIndigo,
                                thumbColor = AccentTeal
                            )
                        )

                        // Recurrence Selection
                        Text("Recurrence", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("None", "Daily", "Weekly").forEach { rec ->
                                val isSel = newRecurrence == rec
                                FilterChip(
                                    selected = isSel,
                                    onClick = { newRecurrence = rec },
                                    label = { Text(rec) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTitle.isNotEmpty()) {
                                viewModel.addTask(
                                    title = newTitle,
                                    description = newDescription,
                                    priority = newPriority,
                                    category = newCategory,
                                    estimatedMinutes = newDuration,
                                    recurrence = newRecurrence
                                )
                                // Reset form
                                newTitle = ""
                                newDescription = ""
                                newCategory = "Work"
                                newPriority = 1
                                newDuration = 30
                                newRecurrence = "None"
                                showCreateDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo)
                    ) {
                        Text("Save Task", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // TASK DETAIL / SUBTASK VIEW DIALOG
        if (taskToViewDetail != null) {
            val task = taskToViewDetail!!
            AlertDialog(
                onDismissRequest = { taskToViewDetail = null },
                title = { Text(task.title, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(task.description.ifEmpty { "No description provided." })
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                        Text("Task Parameters:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Category: ${task.category}", style = MaterialTheme.typography.bodySmall)
                        Text("Priority: ${if (task.priority == 2) "High" else if (task.priority == 1) "Medium" else "Low"}", style = MaterialTheme.typography.bodySmall)
                        Text("Time Allocated: ${task.estimatedMinutes} minutes", style = MaterialTheme.typography.bodySmall)
                        Text("Recurrence: ${task.recurrence}", style = MaterialTheme.typography.bodySmall)
                        if (task.isAiPlanned) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, "AI", tint = AccentIndigo, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Planned automatically by Gemini", fontSize = 11.sp, color = AccentIndigo)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { taskToViewDetail = null }) {
                        Text("Done")
                    }
                }
            )
        }
    }
}
