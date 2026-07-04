package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CompletionRingChart
import com.example.ui.components.FocusTimeBarChart
import com.example.ui.components.MoodTrendLineChart
import com.example.ui.theme.*
import com.example.ui.viewmodel.PlannerViewModel

@Composable
fun AnalyticsScreen(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsState()
    val habits by viewModel.allHabits.collectAsState()
    val moodLogs by viewModel.allMoodLogs.collectAsState()
    val metrics by viewModel.allProductivityMetrics.collectAsState()

    // Calculate dynamic values for charts
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val taskCompletionRate = if (totalTasks > 0) completedTasks.toFloat() / totalTasks.toFloat() else 0f

    // 1. Focus Time Bar Chart Data (last 5 days simulated or derived from local metrics)
    val barChartData = remember(metrics) {
        if (metrics.isEmpty()) {
            listOf(
                "Mon" to 30f,
                "Tue" to 45f,
                "Wed" to 60f,
                "Thu" to 20f,
                "Fri" to 75f
            )
        } else {
            metrics.take(5).reversed().map {
                val dayLabel = it.dateString.substringAfterLast("-") // Just get day index
                "Day $dayLabel" to it.focusTimeMinutes.toFloat()
            }
        }
    }

    // 2. Mood Trend Line Chart Data
    val moodTrendData = remember(moodLogs) {
        if (moodLogs.isEmpty()) {
            listOf(4f, 5f, 3f, 2f, 4f, 5f, 5f) // Default Focused/Happy states
        } else {
            moodLogs.take(7).reversed().map {
                when (it.mood) {
                    "Happy" -> 5f
                    "Focused" -> 4f
                    "Motivated" -> 3f
                    "Tired" -> 2f
                    "Stressed" -> 1f
                    else -> 3f
                }
            }
        }
    }

    // Summary Calculations
    val totalFocusedMins = tasks.filter { it.isCompleted }.sumOf { it.estimatedMinutes }
    val maxHabitStreak = habits.maxOfOrNull { it.streak } ?: 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Performance Cockpit",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Analyze your cognitive bandwidth and schedules",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Big summary cards row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Focus Time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${totalFocusedMins}m",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentIndigo
                    )
                    Text("Minutes total", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Best Streak", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${maxHabitStreak}🔥",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentAmber
                    )
                    Text("Days active", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Ring Completion rate card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompletionRingChart(
                    percentage = taskCompletionRate,
                    title = "",
                    modifier = Modifier.size(110.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Work Efficiency", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Calculated from completed vs overall tasks.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$completedTasks done out of $totalTasks",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentTeal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Focus Time Bar Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                FocusTimeBarChart(data = barChartData, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mood Trend spline-like Line Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                MoodTrendLineChart(data = moodTrendData, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI Cognitive Insights
        Card(
            colors = CardDefaults.cardColors(containerColor = CardObsidian),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, CardBorderObsidian),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, "Sparkle", tint = AccentIndigo, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "AI Bandwidth Insight",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (taskCompletionRate > 0.7f) {
                        "Your efficiency levels are highly optimal today! You complete tasks 25% faster when logging mood as 'Focused' in the morning. Keep it up!"
                    } else {
                        "Your schedule is heavily stacked. We suggest deferring low-priority tasks to the weekend or utilizing high-focus time blocks between 10:00 AM and 1:00 PM."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimaryDark,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
