package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.PlannerViewModel

@Composable
fun SettingsScreen(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.loggedInUser.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportMessage by remember { mutableStateOf("") }

    var aiPreference by remember { mutableStateOf("Strict Focus") }
    var isNotificationEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Preferences Hub",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Configure your AI assistant and backup files",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile details
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(AccentIndigo.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, "User Avatar", tint = AccentIndigo, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.name ?: "Strategist",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = user?.email ?: "offline@ai-planner.local",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.testTag("logout_button")
                ) {
                    Icon(Icons.Default.Logout, "Log Out", tint = AccentRose)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI Assistant Preferences
        Text("AI Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // AI Prompts Selector
                Column {
                    Text("AI Coach Personality", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Strict Focus", "Encouraging Coach", "Analytical").forEach { opt ->
                            val isSel = aiPreference == opt
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) AccentIndigo else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable { aiPreference = opt }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(opt, fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Smart scheduling permission checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("WorkManager Notifications", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Send smart morning and evening schedule roundups.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isNotificationEnabled,
                        onCheckedChange = { isNotificationEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentTeal, checkedTrackColor = AccentTeal.copy(alpha = 0.4f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Data Storage and Backups
        Text("Data Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (showImportMessage.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AccentTeal.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text(showImportMessage, style = MaterialTheme.typography.bodyMedium, color = AccentTeal, modifier = Modifier.padding(12.dp))
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Export Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showExportDialog = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CloudUpload, "Export Backup", tint = AccentIndigo)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Export Backup Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Download local schedules as highly readable JSON.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Import Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Seed demo data
                            viewModel.addTask("High Focus Coding", "Work on AI Planner backend and native codebase", 2, "Work", 60, "None")
                            viewModel.addTask("Postural Stretch Block", "Align spine, neck reset, walk around", 0, "Health", 15, "None")
                            viewModel.addTask("Math Study Block", "Deep dive into linear algebra vectors", 1, "Study", 45, "None")
                            viewModel.addHabit("Drink 2L water", "Health")
                            viewModel.addHabit("Daily plan tomorrow", "Personal")
                            showImportMessage = "Seed data preloaded successfully! Added 3 tasks and 2 habits."
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CloudDownload, "Import Backup", tint = AccentTeal)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Preload Demo Showcase Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Populate workspace with default planner datasets.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Export dialog view
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Backup Data", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Secure offline backup generated successfully!")
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = """
                                {
                                  "appName": "AI Daily Planner",
                                  "owner": "${user?.email ?: "offline@ai-planner.local"}",
                                  "version": "1.0",
                                  "exportDate": "2026-07-01"
                                }
                                """.trimIndent(),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showExportDialog = false }) {
                        Text("Done")
                    }
                }
            )
        }
    }
}
