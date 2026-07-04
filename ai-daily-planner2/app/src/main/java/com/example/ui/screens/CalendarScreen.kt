package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.PlannerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf("Weekly") } // Daily, Weekly, Monthly
    val tasks by viewModel.allTasks.collectAsState()

    var isSyncedWithGoogle by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf("") }

    val todayStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Calendar header title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Calendar Workspace",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$viewMode view • $todayStr",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Sync google button
            Button(
                onClick = {
                    isSyncedWithGoogle = !isSyncedWithGoogle
                    syncMessage = if (isSyncedWithGoogle) {
                        "Google Calendar sync successful! Pulled 2 events."
                    } else "Google Calendar connection disconnected."
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSyncedWithGoogle) AccentTeal else AccentIndigo
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Sync, "Sync", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isSyncedWithGoogle) "Connected" else "Sync Google")
            }
        }

        if (syncMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = AccentTeal.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, "Done", tint = AccentTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(syncMessage, style = MaterialTheme.typography.bodyMedium, color = AccentTeal)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Toggle Segmented-style chips
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            listOf("Daily", "Weekly", "Monthly").forEach { mode ->
                val isSelected = viewMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { viewMode = mode }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = mode,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) AccentIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Week Strip selector
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    val dates = listOf("28", "29", "30", "1", "2", "3", "4") // simulated current days of the week surrounding Jul 1 2026
                    days.zip(dates).forEach { (day, date) ->
                        val isToday = date == "1" // Wednesday Jul 1
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isToday) AccentIndigo else Color.Transparent)
                                .border(1.dp, if (isToday) AccentIndigo else Color.Transparent, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                day,
                                fontSize = 11.sp,
                                color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                date,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isToday) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Timeline scheduling grid simulation (e.g. 9am to 6pm hourly slots)
        Text("Daily Time Slots", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        val hours = listOf(
            "09:00 AM" to "Morning Routine",
            "11:00 AM" to "High Focus Deep Work",
            "01:00 PM" to "Lunch & Recharge Break",
            "03:00 PM" to "Collaboration & Calls",
            "05:00 PM" to "Daily Wrapup & Review",
            "07:00 PM" to "Healthy Meal & Exercise"
        )

        hours.forEach { (timeStr, label) ->
            // Match if any user task can fit in here
            val matchedTasks = tasks.filter { !it.isCompleted && it.category == when (timeStr) {
                "11:00 AM" -> "Work"
                "03:00 PM" -> "Study"
                "07:00 PM" -> "Health"
                else -> "Personal"
            }}

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.width(85.dp)) {
                        Text(timeStr, fontWeight = FontWeight.Bold, color = AccentIndigo, fontSize = 12.sp)
                        Text("1 Hour", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    VerticalDivider()

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (matchedTasks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            matchedTasks.take(2).forEach { t ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AccentIndigo.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Task, "Task", tint = AccentIndigo, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(t.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentIndigo)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        } else {
                            Text("No tasks scheduled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    thickness: androidx.compose.ui.unit.Dp = 1.dp
) {
    Box(
        modifier
            .fillMaxHeight()
            .width(thickness)
            .background(color)
    )
}
