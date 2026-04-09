package com.persona.companion.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = "",
    val effect: String? = "",
    val price: String? = "",
    val sellPrice: String? = "",
    val location: String? = "",
    val attack: String? = null,
    val accuracy: String? = null,
    val category: String? = "General",
    val gameId: String = "", // To distinguish between p3/p4/p5 etc.
    val episodeAigis: Boolean? = false
)

data class ItemData(
    val items: List<Item>
)
