package com.example.omega_tracker.data


import android.net.Uri
import android.util.Log
import com.example.omega_tracker.data.local_data.TaskType
import com.example.omega_tracker.entity.Task
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.*
import kotlin.time.Duration

@JsonClass(generateAdapter = true)
data class AppDataTask(
    @Json(name = "summary") override var summary: String,
    @Json(name = "description") override var description: String,
    @Json(name = "id") override var id: String,
    @Json(name = "nameProject") override var nameProject: String,
    @Json(name = "iconUri") override var iconUrl: Uri?,
    @Json(name = "currentState") override var currentState: String,
    @ConvertCurrentTime @Json(name = "estimate") var estimate: Duration,
    @ConvertStartDate @Json(name = "startDate") var startDate: LocalDateTime?,
    @ConvertCurrentTime @Json(name = "timeSpent") var timeSpent: Duration,
    @ConvertCurrentTime @Json(name = "timeLeft") var timeLeft: Duration,
    @Json(name = "taskStatus") override var taskStatus: TaskStatus,
    @Json(name = "taskLaunchTime") override val taskLaunchTime: LocalDateTime?,
    @Json(name = "taskType") override var taskType: TaskType,
) : Task {
    constructor(task: Task) : this(
        task.summary,
        task.description,
        task.id,
        task.nameProject,
        task.iconUrl,
        task.currentState,
        task.evaluate,
        task.onset,
        task.usedTime,
        task.remainingTime,
        task.taskStatus,
        task.taskLaunchTime,
        task.taskType
    )

    override var evaluate: Duration
        get() = estimate
        set(value) {}

    override var onset: LocalDateTime?
        get() = startDate
        set(value) {}

    override var usedTime: Duration
        get() = timeSpent
        set(value) {}

    override var remainingTime: Duration
        get() {
            return timeLeft
        }
        set(value) {}
}

enum class TaskStatus {
    Open, Run, Pause
}
