package com.persona.companion.models

data class Skill(
    val name: String,
    val element: String,
    val cost: Int,
    val costType: String = "",
    val effect: String,
    val target: String = "",
    val rank: Int = 0
)
