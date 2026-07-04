package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PlannerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: PlannerViewModel = viewModel()
                val loggedInUser by viewModel.loggedInUser.collectAsState()
                val activeTab by viewModel.activeTab.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (loggedInUser != null) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .navigationBarsPadding()
                                    .testTag("app_navigation_bar")
                            ) {
                                val tabs = listOf(
                                    Triple("Dashboard", Icons.Default.Home, "Dashboard"),
                                    Triple("Tasks", Icons.Default.Task, "Tasks"),
                                    Triple("Chat", Icons.Default.AutoAwesome, "AI Chat"),
                                    Triple("Calendar", Icons.Default.CalendarToday, "Calendar"),
                                    Triple("Habits", Icons.Default.Repeat, "Habits"),
                                    Triple("Analytics", Icons.Default.ShowChart, "Insights"),
                                    Triple("Settings", Icons.Default.Settings, "Settings")
                                )

                                tabs.forEach { (tabName, icon, label) ->
                                    val isSelected = activeTab == tabName
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { viewModel.setActiveTab(tabName) },
                                        icon = { Icon(icon, contentDescription = label) },
                                        label = { Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        ),
                                        modifier = Modifier.testTag("nav_item_${tabName.lowercase()}")
                                    )
                                }
                            }
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (loggedInUser == null) {
                            AuthScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                        } else {
                            when (activeTab) {
                                "Dashboard" -> DashboardScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                                "Tasks" -> TaskManagementScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                                "Chat" -> ChatbotScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                                "Calendar" -> CalendarScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                                "Habits" -> HabitScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                                "Analytics" -> AnalyticsScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                                "Settings" -> SettingsScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                                else -> DashboardScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
    }
}
