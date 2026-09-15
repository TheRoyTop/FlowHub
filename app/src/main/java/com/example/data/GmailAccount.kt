package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gmail_accounts")
data class GmailAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val displayName: String,
    val label: String = "Personal",
    val avatarColorHex: String = "#8083FF",
    val isActive: Boolean = false,
    val orderIndex: Int = 0,
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)
