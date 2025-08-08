package com.example.smartzone.entities

import com.google.firebase.Timestamp

data class Document(
    val id: String = "",
    val name: String = "",
    val fileUri: String = "",
    val uploadedAt: Timestamp = Timestamp.now()
)

