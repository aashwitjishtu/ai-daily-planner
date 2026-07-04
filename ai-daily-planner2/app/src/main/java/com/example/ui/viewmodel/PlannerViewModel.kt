package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.database.*
import com.example.data.repository.PlannerRepository
import com.example.data.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = PlannerRepository(
        userDao = database.userDao(),
        taskDao = database.taskDao(),
        habitDao = database.habitDao(),
        moodDao = database.moodDao(),
        chatDao = database.chatDao(),
        analyticsDao = database.analyticsDao()
    )

    // --- UI Navigation State ---
    private val _activeTab = MutableStateFlow("Dashboard") // Dashboard, Calendar, Tasks, Habits, Chat, Analytics, Settings
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    // --- Authentication States ---
    val loggedInUser: StateFlow<UserAccount?> = repository.loggedInUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading = _isAuthLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                val success = repository.loginUser(email, password)
                if (!success) {
                    _authError.value = "Invalid login credentials."
                }
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Connection error."
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun register(email: String, password: String, name: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                val success = repository.registerUser(email, password, name)
                if (!success) {
                    _authError.value = "Registration failed."
                }
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Connection error."
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    // --- Tasks State & Filtering ---
    val allTasks: StateFlow<List<Task>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val completedTasks: StateFlow<List<Task>> = repository.completedTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val archivedTasks: StateFlow<List<Task>> = repository.archivedTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _taskSearchQuery = MutableStateFlow("")
    val taskSearchQuery = _taskSearchQuery.asStateFlow()

    private val _taskCategoryFilter = MutableStateFlow("All")
    val taskCategoryFilter = _taskCategoryFilter.asStateFlow()

    private val _taskPriorityFilter = MutableStateFlow(-1) // -1 is All
    val taskPriorityFilter = _taskPriorityFilter.asStateFlow()

    val filteredTasks: StateFlow<List<Task>> = combine(
        allTasks, _taskSearchQuery, _taskCategoryFilter, _taskPriorityFilter
    ) { tasks, query, cat, priority ->
        tasks.filter { task ->
            val matchQuery = task.title.contains(query, ignoreCase = true) || task.description.contains(query, ignoreCase = true)
            val matchCat = cat == "All" || task.category == cat
            val matchPriority = priority == -1 || task.priority == priority
            matchQuery && matchCat && matchPriority
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTaskSearchQuery(query: String) {
        _taskSearchQuery.value = query
    }

    fun setTaskCategoryFilter(category: String) {
        _taskCategoryFilter.value = category
    }

    fun setTaskPriorityFilter(priority: Int) {
        _taskPriorityFilter.value = priority
    }

    fun addTask(title: String, description: String, priority: Int, category: String, estimatedMinutes: Int, recurrence: String, dueDate: Long = 0L) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                priority = priority,
                category = category,
                estimatedMinutes = estimatedMinutes,
                recurrence = recurrence,
                dueDate = if (dueDate == 0L) System.currentTimeMillis() + (24 * 3600 * 1000) else dueDate
            )
            repository.insertTask(task)
            repository.updateProductivityMetrics(getTodayDateString(), totalTaskDelta = 1)
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updated)
            val compDelta = if (updated.isCompleted) 1 else -1
            repository.updateProductivityMetrics(
                getTodayDateString(),
                taskCompletionDelta = compDelta,
                focusTimeDelta = if (updated.isCompleted) task.estimatedMinutes else 0
            )
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            val tcDelta = if (task.isCompleted) -1 else 0
            repository.updateProductivityMetrics(getTodayDateString(), taskCompletionDelta = tcDelta, totalTaskDelta = -1)
        }
    }

    fun archiveTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isArchived = true))
        }
    }

    // --- Habits State ---
    val allHabits: StateFlow<List<Habit>> = repository.allHabits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addHabit(name: String, category: String) {
        viewModelScope.launch {
            val habit = Habit(name = name, category = category)
            repository.insertHabit(habit)
        }
    }

    fun toggleHabitToday(habit: Habit) {
        viewModelScope.launch {
            val today = getTodayDateString()
            if (habit.isCompletedToday) {
                // Uncomplete
                val updated = habit.copy(
                    isCompletedToday = false,
                    streak = (habit.streak - 1).coerceAtLeast(0),
                    totalCompletions = (habit.totalCompletions - 1).coerceAtLeast(0)
                )
                repository.updateHabit(updated)
            } else {
                repository.completeHabitToday(habit, today)
            }
        }
    }

    // --- Mood State ---
    val allMoodLogs: StateFlow<List<MoodLog>> = repository.allMoodLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun logMoodToday(mood: String, note: String) {
        viewModelScope.launch {
            repository.logMood(mood, note, getTodayDateString())
        }
    }

    // --- AI Suggestions & Day Planning ---
    private val _aiSuggestion = MutableStateFlow("Your AI assistant is generating advice to jumpstart your morning...")
    val aiSuggestion = _aiSuggestion.asStateFlow()

    private val _isAiPlanning = MutableStateFlow(false)
    val isAiPlanning = _isAiPlanning.asStateFlow()

    fun generateAiDayPlan() {
        viewModelScope.launch(Dispatchers.IO) {
            _isAiPlanning.value = true
            val tasksText = allTasks.value.filter { !it.isCompleted }.joinToString("\n") { "- ${it.title} (${it.category}, priority=${it.priority})" }
            val habitsText = allHabits.value.joinToString("\n") { "- ${it.name} (${it.category})" }
            val moodText = allMoodLogs.value.firstOrNull()?.mood ?: "Not logged yet"

            val prompt = """
                Analyze the following user data to generate a compact, hourly daily schedule and 1-2 powerful productivity tips.
                Current Mood: $moodText
                Uncompleted Tasks:
                $tasksText
                
                Daily Habits:
                $habitsText
                
                Please structure the daily schedule beautifully. Keep suggestions encouraging and focused on mood alignment. Output directly in clean markdown text.
            """.trimIndent()

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val req = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    systemInstruction = Content(parts = listOf(Part(text = "You are a professional AI productivity architect. Output schedules clearly and concisely.")))
                )
                val response = GeminiRetrofitClient.service.generateContent(apiKey, req)
                val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!textResponse.isNullOrEmpty()) {
                    _aiSuggestion.value = textResponse
                } else {
                    _aiSuggestion.value = "Schedule generated! Log a habit to maintain your momentum."
                }
            } catch (e: Exception) {
                _aiSuggestion.value = "AI Schedule Fallback Plan:\n- 9:00 AM: Focus on work tasks\n- 12:30 PM: Midday walk and habit check\n- 3:00 PM: High priority task sprint\n- 6:00 PM: Log mood and plan tomorrow.\n\n(Error reaching Gemini: ${e.localizedMessage})"
            } finally {
                _isAiPlanning.value = false
            }
        }
    }

    // --- Chatbot Screen State & natural language commands ---
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading = _isChatLoading.asStateFlow()

    fun sendChatMessage(text: String) {
        if (text.trim().isEmpty()) return
        viewModelScope.launch {
            repository.insertChatMessage("User", text)
            _isChatLoading.value = true

            // Build conversation history for context
            val history = chatMessages.value.takeLast(10).map {
                Content(parts = listOf(Part(text = it.text)), role = if (it.sender == "User") "user" else "model")
            }

            val systemPrompt = """
                You are 'PlannerAI', the conversational assistant inside the AI Daily Planner app.
                You can help users create schedules, answer productivity questions, log habits, and execute tasks.
                
                We support direct command execution via structured action tags. If the user asks to add, complete, or reschedule a task, speak in a friendly conversational voice, then APPEND exactly one matching ACTION tag at the absolute end of your response:
                - To add a task: [ACTION:ADD_TASK|title|category|priority(0,1,2)|estimatedMinutes]
                - To complete a task: [ACTION:COMPLETE_TASK|title]
                - To move/reschedule a task: [ACTION:MOVE_TASK|title|timeString]
                
                Examples:
                - User: "Add gym at 6pm"
                  Response: "Excellent choice! I've added Gym to your list for this evening. [ACTION:ADD_TASK|Gym|Health|1|45]"
                - User: "Move gym to the evening"
                  Response: "Got it, I'll update the gym schedule. [ACTION:MOVE_TASK|Gym|6:00 PM]"
                - User: "I finished reading"
                  Response: "Incredible job! I'll check that task off your list. [ACTION:COMPLETE_TASK|Reading]"
                
                Otherwise, respond with helpful, friendly productivity tips without tags.
            """.trimIndent()

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val apiContents = history + Content(parts = listOf(Part(text = text)), role = "user")
                val req = GenerateContentRequest(
                    contents = apiContents,
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                )
                val response = GeminiRetrofitClient.service.generateContent(apiKey, req)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I am having trouble understanding right now. Please try again."

                _isChatLoading.value = false
                repository.insertChatMessage("AI", reply)

                // Parse action tags if present
                parseAndExecuteActionTag(reply)
            } catch (e: Exception) {
                _isChatLoading.value = false
                repository.insertChatMessage("AI", "I had an issue connecting to the AI brain. Let's try that again. Details: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun parseAndExecuteActionTag(text: String) {
        val regex = Regex("\\[ACTION:([^\\]]+)\\]")
        val match = regex.find(text)
        if (match != null) {
            val actionBody = match.groupValues[1]
            val parts = actionBody.split("|")
            if (parts.isNotEmpty()) {
                val actionType = parts[0]
                when (actionType) {
                    "ADD_TASK" -> {
                        if (parts.size >= 5) {
                            val title = parts[1]
                            val cat = parts[2]
                            val priority = parts[3].toIntOrNull() ?: 1
                            val duration = parts[4].toIntOrNull() ?: 30
                            addTask(title, "Created via AI Chatbot", priority, cat, duration, "None")
                        }
                    }
                    "COMPLETE_TASK" -> {
                        if (parts.size >= 2) {
                            val title = parts[1]
                            val matchedTask = allTasks.value.firstOrNull { it.title.contains(title, ignoreCase = true) && !it.isCompleted }
                            if (matchedTask != null) {
                                toggleTaskCompleted(matchedTask)
                            }
                        }
                    }
                    "MOVE_TASK" -> {
                        if (parts.size >= 3) {
                            val title = parts[1]
                            val timeStr = parts[2]
                            val matchedTask = allTasks.value.firstOrNull { it.title.contains(title, ignoreCase = true) }
                            if (matchedTask != null) {
                                val updated = matchedTask.copy(description = matchedTask.description + " (Scheduled for $timeStr)")
                                repository.updateTask(updated)
                            }
                        }
                    }
                }
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // --- Analytics / Productivity Metrics State ---
    val allProductivityMetrics: StateFlow<List<ProductivityMetric>> = repository.allProductivityMetrics.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Date Helpers ---
    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    init {
        // Automatically fetch initial plan
        generateAiDayPlan()
    }
}
