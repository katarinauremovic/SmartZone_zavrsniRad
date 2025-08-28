package com.example.smartzone.entities

data class RepeatRule(
    val freq: String = "WEEKLY",
    val interval: Int = 1,
    val untilDateUtc: Long? = null
)