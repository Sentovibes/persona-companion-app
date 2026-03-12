package com.persona.companion.models

/**
 * Represents a classroom question and its answer
 */
data class ClassroomQuestion(
    val date: String,              // e.g., "4/18", "May 6"
    val question: String,          // The question text
    val answer: String,            // The correct answer
    val type: QuestionType = QuestionType.CLASS,  // Class question or exam
    val subject: String? = null,   // Optional subject (for exams)
    val isExclusive: Boolean = false  // P4G Exclusive or P5R Exclusive flag
)

enum class QuestionType {
    CLASS,      // Regular classroom question
    EXAM,       // Exam question
    MIDTERM,    // Midterm exam
    FINAL       // Final exam
}

/**
 * Container for all classroom questions in a game
 */
data class ClassroomData(
    val gameId: String,
    val questions: List<ClassroomQuestion>
)
