package com.persona.companion.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Request(
    val name: String,
    val available: String? = null,
    val reward: String? = null,
    
    // P3R / P4G / P3P / P3FES
    val id: String? = null,
    val deadline: String? = null,
    val remarks: String? = null,
    val details: String? = null,
    val notes: String? = null,
    val giver: String? = null,
    val quest_giver: String? = null, // P4G specific
    val category: String? = null,
    
    // P5R
    val intel_required: String? = null,
    val target: String? = null,
    val target_name: String? = null,
    val demon_form: String? = null,
    val target_enemy: String? = null,
    val location: String? = null,
    val difficulty: String? = null,
    val weakness: String? = null,
    val sortOrder: Int? = null,
    val episodeAigis: Boolean? = false
) : Parcelable
