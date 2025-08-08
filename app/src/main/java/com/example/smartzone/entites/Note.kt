package com.example.smartzone.entities

import com.google.firebase.Timestamp

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: Timestamp = Timestamp.now()
)
