package com.persona.companion.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Request(
    val name: String,
    val available: String,
    val reward: String,
    
    // P3R / P4G
    val id: String? = null,
    val deadline: String? = null,
    val remarks: String? = null,
    val giver: String? = null,
    val quest_giver: String? = null, // P4G specific
    val category: String? = null,
    
    // P5R
    val intel_required: String? = null,
    val target_name: String? = null,
    val target_enemy: String? = null,
    val location: String? = null,
    val difficulty: String? = null
) : Parcelable
