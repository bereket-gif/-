package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mezmur_cache")
data class MezmurEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val category: String,
    val lyrics: String,
    val numberGeez: String,
    val numberInt: Int,
    val isFavorite: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
