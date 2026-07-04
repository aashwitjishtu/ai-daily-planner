package com.example.data.repository

import com.example.data.database.*
import com.example.data.api.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.IOException

class PlannerRepository(
    private val userDao: UserDao,
    private val taskDao: TaskDao,
    private val habitDao: HabitDao,
    private val moodDao: MoodDao,
    private val chatDao: ChatDao,
    private val analyticsDao: AnalyticsDao
) {
    // --- Auth & User ---
    val loggedInUser: Flow<UserAccount?> = userDao.getLoggedInUser()

    suspend fun registerUser(email: String, password: String, name: String): Boolean {
        return try {
            val response = BackendRetrofitClient.service.register(RegisterRequest(email, password, name))
            val user = UserAccount(
                email = response.email,
                name = response.name,
                isLoggedIn = true,
                token = "Bearer ${response.access_token}"
            )
            userDao.logoutAllUsers()
            userDao.insertUser(user)
            true
        } catch (e: Exception) {
            // Local fallback registration
            val user = UserAccount(email = email, name = name, isLoggedIn = true, token = "Local-Token-Offline")
            userDao.logoutAllUsers()
            userDao.insertUser(user)
            true
        }
    }

    suspend fun loginUser(email: String, password: String): Boolean {
        return try {
            val response = BackendRetrofitClient.service.login(LoginRequest(email, password))
            val user = UserAccount(
                email = response.email,
                name = response.name,
                isLoggedIn = true,
                token = "Bearer ${response.access_token}"
            )
            userDao.logoutAllUsers()
            userDao.insertUser(user)
            true
        } catch (e: Exception) {
            // Check local DB if user already registered offline
            val localUser = userDao.getUserByEmail(email)
            if (localUser != null) {
                userDao.logoutAllUsers()
                userDao.insertUser(localUser.copy(isLoggedIn = true))
                true
            } else {
                // Auto create local offline demo account
                val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                val user = UserAccount(email = email, name = name, isLoggedIn = true, token = "Local-Token-Offline")
                userDao.logoutAllUsers()
                userDao.insertUser(user)
                true
            }
        }
    }

    suspend fun logout() {
        userDao.logoutAllUsers()
    }

    // --- Tasks ---
    val allTasks: Flow<List<Task>> = taskDao.getAllTasksFlow()
    val completedTasks: Flow<List<Task>> = taskDao.getCompletedTasksFlow()
    val archivedTasks: Flow<List<Task>> = taskDao.getArchivedTasksFlow()

    suspend fun insertTask(task: Task): Long {
        val id = taskDao.insertTask(task)
        triggerSync()
        return id
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
        triggerSync()
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
        triggerSync()
    }

    suspend fun deleteTaskById(id: Long) {
        taskDao.deleteTaskById(id)
        triggerSync()
    }

    // --- Habits ---
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabitsFlow()

    suspend fun insertHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun completeHabitToday(habit: Habit, dateString: String) {
        val log = HabitLog(habitId = habit.id, dateString = dateString, isCompleted = true)
        habitDao.insertHabitLog(log)

        val updatedStreak = if (habit.lastCompletedDate != dateString) habit.streak + 1 else habit.streak
        val updatedHabit = habit.copy(
            streak = updatedStreak,
            totalCompletions = habit.totalCompletions + 1,
            lastCompletedDate = dateString,
            isCompletedToday = true
        )
        habitDao.updateHabit(updatedHabit)
        updateProductivityMetrics(dateString, focusTimeDelta = 10) // Reward habit completion with 10 focus mins
    }

    fun getHabitLogsForDate(dateString: String): Flow<List<HabitLog>> {
        return habitDao.getHabitLogsForDateFlow(dateString)
    }

    // --- Mood ---
    val allMoodLogs: Flow<List<MoodLog>> = moodDao.getAllMoodLogsFlow()

    suspend fun logMood(mood: String, note: String, dateString: String) {
        val log = MoodLog(timestamp = System.currentTimeMillis(), mood = mood, note = note)
        moodDao.insertMoodLog(log)
        updateProductivityMetrics(dateString, newMood = mood)
    }

    // --- Chatbot ---
    val chatMessages: Flow<List<ChatMessage>> = chatDao.getChatMessagesFlow()

    suspend fun insertChatMessage(sender: String, text: String) {
        chatDao.insertMessage(ChatMessage(sender = sender, text = text, timestamp = System.currentTimeMillis()))
    }

    suspend fun clearChat() {
        chatDao.clearChatHistory()
    }

    // --- Analytics & Productivity ---
    val allProductivityMetrics: Flow<List<ProductivityMetric>> = analyticsDao.getAllMetricsFlow()

    suspend fun updateProductivityMetrics(dateString: String, taskCompletionDelta: Int = 0, totalTaskDelta: Int = 0, focusTimeDelta: Int = 0, newMood: String? = null) {
        val existing = analyticsDao.getMetricForDate(dateString)
        val metric = if (existing != null) {
            val tc = (existing.tasksCompleted + taskCompletionDelta).coerceAtLeast(0)
            val tt = (existing.totalTasksCount + totalTaskDelta).coerceAtLeast(0)
            existing.copy(
                tasksCompleted = tc,
                totalTasksCount = tt,
                focusTimeMinutes = (existing.focusTimeMinutes + focusTimeDelta).coerceAtLeast(0),
                dominantMood = newMood ?: existing.dominantMood,
                completionRate = if (tt > 0) tc.toFloat() / tt.toFloat() else 0f
            )
        } else {
            val tc = taskCompletionDelta.coerceAtLeast(0)
            val tt = totalTaskDelta.coerceAtLeast(0)
            ProductivityMetric(
                dateString = dateString,
                tasksCompleted = tc,
                totalTasksCount = tt,
                focusTimeMinutes = focusTimeDelta.coerceAtLeast(0),
                dominantMood = newMood ?: "Focused",
                completionRate = if (tt > 0) tc.toFloat() / tt.toFloat() else 0f
            )
        }
        analyticsDao.insertMetric(metric)
    }

    // --- Server Synchronizer ---
    suspend fun triggerSync() {
        try {
            val user = userDao.getLoggedInUser().firstOrNull() ?: return
            if (user.token.isEmpty() || user.token == "Local-Token-Offline") return

            val tasks = taskDao.getAllTasksFlow().firstOrNull() ?: return
            val backendTasks = tasks.map {
                BackendTaskSync(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    isCompleted = it.isCompleted,
                    isArchived = it.isArchived,
                    dueDate = it.dueDate,
                    priority = it.priority,
                    category = it.category,
                    recurrence = it.recurrence,
                    subtasksRaw = it.subtasksRaw
                )
            }
            BackendRetrofitClient.service.syncTasks(user.token, backendTasks)
        } catch (e: Exception) {
            // Silent error - continue offline
        }
    }
}
