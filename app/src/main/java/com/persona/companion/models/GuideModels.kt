package com.persona.companion.models

data class NewsUpdate(
    val gameId: String,
    val version: String,
    val date: String,
    val title: String,
    val category: String,
    val summary: String,
    val details: String
)

data class QuestGuideGiver(
    val giver: String,
    val gameId: String,
    val quests: List<QuestGuideItem>
)

data class QuestGuideItem(
    val id: String,
    val title: String,
    val requirement: String,
    val reward: String,
    val walkthrough: String
)

data class BossGuideGame(
    val gameId: String,
    val bosses: List<BossGuideItem>
)

data class BossGuideItem(
    val name: String,
    val level: Int,
    val location: String,
    val party: String,
    val weaknesses: String,
    val resistances: String,
    val strategy: String,
    val buildPrep: String
)

data class DayGuideGame(
    val gameId: String,
    val months: List<DayGuideMonth>
)

data class DayGuideMonth(
    val month: String,
    val overview: String,
    val days: List<DayGuideDay>
)

// category: "deadline" | "exam" | "story" | "unlock" | "event" | "tip"
data class DayGuideDay(
    val date: String,
    val title: String,
    val category: String,
    val description: String
)
