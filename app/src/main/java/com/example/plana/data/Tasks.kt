package com.example.plana.data

import android.app.ActivityManager.TaskDescription

data class TaskItem(
    val taskName: String,
    val description: String,
    val isChecked: Boolean
)