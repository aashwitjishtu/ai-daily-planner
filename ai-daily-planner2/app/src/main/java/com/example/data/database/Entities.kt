package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey val email: String,
    val name: String,
    val profileImage: String = "",
    val isLoggedIn: Boolean = false,
    val token: String = "",
    val aiPreferences: String = "Balanced"
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val isArchived: Boolean = false,
    val dueDate: Long = 0L, // timestamp
    val priority: Int = 1, // 0 = Low, 1 = Medium, 2 = High
    val category: String = "Personal", // Work, Personal, Health, Study, Social
    val recurrence: String = "None", // None, Daily, Weekly
    val subtasksRaw: String = "", // Comma-separated or JSON list of "subtask_title|completed"
    val attachmentPath: String = "",
    val estimatedMinutes: Int = 30,
    val plannedStartTime: Long = 0L, // timestamp for AI schedule allocation
    val isAiPlanned: Boolean = false
) {
    fun getSubtasksList(): List<Pair<String, Boolean>> {
        if (subtasksRaw.isEmpty()) return emptyList()
        return subtasksRaw.split(";").mapNotNull {
            val parts = it.split("|")
            if (parts.size == 2) {
                parts[0] to (parts[1].toBoolean())
            } else null
        }
    }

    companion object {
        fun createRawSubtasks(list: List<Pair<String, Boolean>>): String {
            return list.joinToString(";") { "${it.first}|${it.second}" }
        }
    }
}

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "Health",
    val streak: Int = 0,
    val totalCompletions: Int = 0,
    val lastCompletedDate: String = "", // yyyy-MM-dd
    val isCompletedToday: Boolean = false
)

@Entity(tableName = "habit_logs")
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val dateString: String, // yyyy-MM-dd
    val isCompleted: Boolean
)

@Entity(tableName = "mood_logs")
data class MoodLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val mood: String, // Happy, Focused, Tired, Stressed, Motivated
    val note: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // User, AI
    val text: String,
    val timestamp: Long
)

@Entity(tableName = "productivity_metrics")
data class ProductivityMetric(
    @PrimaryKey val dateString: String, // yyyy-MM-dd
    val tasksCompleted: Int = 0,
    val totalTasksCount: Int = 0,
    val focusTimeMinutes: Int = 0,
    val dominantMood: String = "Focused",
    val completionRate: Float = 0f
)
