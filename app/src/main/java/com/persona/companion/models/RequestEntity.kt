package com.persona.companion.models

import androidx.room.*

@Entity(tableName = "requests")
data class RequestEntity(
    @PrimaryKey(autoGenerate = true) val dbId: Int = 0,
    val id: String?, // The original ID from JSON (e.g. "01")
    val name: String,
    val reward: String,
    val giver: String?,
    val quest_giver: String?,
    val deadline: String?,
    val location: String?,
    val category: String?,
    val available: String?,
    val intel_required: String?,
    val target_name: String?,
    val target_enemy: String?,
    val remarks: String?,
    val difficulty: String?,
    val gameId: String, // To differentiate between P3R, P4G, P5R
    val sortOrder: Int, // Logical order from JSON
    val isCompleted: Boolean = false,
    val episodeAigis: Boolean? = false
)
