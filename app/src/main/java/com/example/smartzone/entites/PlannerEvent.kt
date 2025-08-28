package com.example.smartzone.entities

data class PlannerEvent(
    val id: String? = null,
    val title: String = "",
    val weekday: Int = 1,
    val startMinutes: Int = 0,
    val reminderMinutesBefore: Int? = 10,
    val timezone: String = java.time.ZoneId.systemDefault().id
)
