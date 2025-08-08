package com.example.smartzone.entities

import com.google.firebase.Timestamp

data class Zone(
    val id: String = "",
    val name: String = "",
    val focus: String = "",
    val createdAt: Timestamp
)
