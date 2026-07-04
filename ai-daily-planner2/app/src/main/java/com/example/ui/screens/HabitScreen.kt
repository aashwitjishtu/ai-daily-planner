package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.PlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.allHabits.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var habitNameInput by remember { mutableStateOf("") }
    var habitCategoryInput by remember { mutableStateOf("Health") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Habits Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Habit Forge",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Create streaks & maintain positive loops",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AccentIndigo)
                    .size(40.dp)
                    .testTag("create_habit_button")
            ) {
                Icon(Icons.Default.Add, "Create Habit", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI Recommendations Section
        Card(
            colors = CardDefaults.cardColors(containerColor = CardObsidian),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, CardBorderObsidian),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, "AI Suggestions", tint = AccentIndigo, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "AI Suggested Habits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "The AI suggests these atomic loops based on top productivity routines:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(16.dp))

                val suggestions = listOf(
                    "Mindfulness Breathing (5m)" to "Health",
                    "Plan Tomorrow's Top 3 Tasks" to "Work",
                    "Stretch & Reset Posture" to "Health",
                    "Hydrate Before Screen Time" to "Health"
                )

                suggestions.forEach { (name, cat) ->
                    val alreadyAdopted = habits.any { it.name.contains(name, ignoreCase = true) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(CardBorderObsidian.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(cat, fontSize = 10.sp, color = AccentTeal)
                        }

                        if (alreadyAdopted) {
                            Icon(Icons.Default.CheckCircle, "Adopted", tint = AccentTeal, modifier = Modifier.size(20.dp))
                        } else {
                            TextButton(
                                onClick = { viewModel.addHabit(name, cat) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Adopt", color = AccentIndigo, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Your Habits Title
        Text("Your Daily Routines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (habits.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = "Empty Habits",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No habits logged yet.", fontWeight = FontWeight.Bold)
                    Text("Adopt one from suggestions above or create a custom one!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        } else {
            habits.forEach { habit ->
                val isDone = habit.isCompletedToday
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDone) AccentTeal.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isDone) AccentTeal else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.toggleHabitToday(habit) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isDone) AccentTeal else MaterialTheme.colorScheme.surfaceVariant)
                                    .size(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDone) Icons.Default.Check else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Toggle completion",
                                    tint = if (isDone) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = habit.name,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Category: ${habit.category}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Streaks details
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${habit.streak}🔥",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AccentAmber
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "L:${habit.totalCompletions}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // CREATE CUSTOM HABIT DIALOG
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Habit Loop", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = habitNameInput,
                        onValueChange = { habitNameInput = it },
                        label = { Text("Habit Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Category", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    val categoriesList = listOf("Health", "Work", "Personal", "Study", "Social")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categoriesList.take(3).forEach { cat ->
                            val isSel = habitCategoryInput == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { habitCategoryInput = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (habitNameInput.isNotEmpty()) {
                            viewModel.addHabit(habitNameInput, habitCategoryInput)
                            habitNameInput = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo)
                ) {
                    Text("Create", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
