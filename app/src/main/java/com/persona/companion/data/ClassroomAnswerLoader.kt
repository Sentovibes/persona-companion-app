package com.persona.companion.data

import android.content.Context
import android.util.Log
import com.persona.companion.models.ClassroomData
import com.persona.companion.models.ClassroomQuestion
import com.persona.companion.models.QuestionType
import org.json.JSONObject

object ClassroomAnswerLoader {
    private const val TAG = "ClassroomAnswerLoader"
    
    /**
     * Load classroom answers for a specific game
     */
    fun loadClassroomAnswers(context: Context, gameId: String): ClassroomData? {
        return try {
            Log.d(TAG, "Loading classroom answers for gameId: $gameId")
            val filename = getClassroomAnswerFilename(gameId)
            if (filename == null) {
                Log.e(TAG, "No classroom answers available for gameId: $gameId")
                return null
            }
            Log.d(TAG, "Loading file: $filename")
            
            val inputStream = try {
                context.assets.open(filename)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open file: $filename", e)
                throw e
            }
            
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            Log.d(TAG, "File loaded, parsing JSON (${jsonString.length} chars)")
            
            val result = parseClassroomAnswersJson(gameId, jsonString)
            Log.d(TAG, "Parsed ${result.questions.size} questions")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error loading classroom answers for $gameId", e)
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get the filename for a game's classroom answers
     * All P3 games share one file, all P4 games share one file, all P5 games share one file
     */
    private fun getClassroomAnswerFilename(gameId: String): String? {
        return when (gameId) {
            "p3fes", "p3p", "p3r" -> "data/classroom/p3_classroom_answers.json"
            "p4", "p4g" -> "data/classroom/p4_classroom_answers.json"
            "p5", "p5r" -> "data/classroom/p5_classroom_answers.json"
            else -> {
                Log.w(TAG, "No classroom answers available for gameId: $gameId")
                null
            }
        }
    }
    
    /**
     * Parse classroom answers JSON data
     * Structure: Month -> Type (Classroom/Exam) -> Date -> Array of Questions
     */
    private fun parseClassroomAnswersJson(gameId: String, jsonString: String): ClassroomData {
        val questions = mutableListOf<ClassroomQuestion>()
        val rootJson = JSONObject(jsonString)
        
        // Determine if this is a Royal/Golden version for filtering
        val isRoyalOrGolden = gameId == "p5r" || gameId == "p4g"
        
        // Iterate through each month
        val months = rootJson.keys()
        while (months.hasNext()) {
            val month = months.next()
            val monthData = rootJson.getJSONObject(month)
            
            // Iterate through types (Classroom, Exam, Midterm, Final)
            val types = monthData.keys()
            while (types.hasNext()) {
                val typeStr = types.next()
                val typeData = monthData.getJSONObject(typeStr)
                
                val questionType = when (typeStr.lowercase()) {
                    "exam" -> QuestionType.EXAM
                    "midterm" -> QuestionType.MIDTERM
                    "final" -> QuestionType.FINAL
                    else -> QuestionType.CLASS
                }
                
                // Iterate through dates
                val dates = typeData.keys()
                while (dates.hasNext()) {
                    val date = dates.next()
                    val questionsArray = typeData.getJSONArray(date)
                    
                    // Parse each question in the array
                    for (i in 0 until questionsArray.length()) {
                        val questionObj = questionsArray.getJSONObject(i)
                        
                        // Check for exclusive flags
                        val isP5RExclusive = questionObj.optBoolean("P5R Exclusive", false)
                        val isP4GExclusive = questionObj.optBoolean("P4G Exclusive", false)
                        val isExclusive = isP5RExclusive || isP4GExclusive
                        
                        // Skip exclusive content if not playing the Royal/Golden version
                        if (isExclusive && !isRoyalOrGolden) {
                            continue
                        }
                        
                        val question = questionObj.optString("Question", "")
                        val answer = questionObj.optString("Answer", "")
                        
                        // If no question text, use answer as the question (for incomplete data)
                        val questionText = if (question.isNotEmpty()) question else "Question not available"
                        
                        if (answer.isNotEmpty()) {
                            questions.add(
                                ClassroomQuestion(
                                    date = date,
                                    question = questionText,
                                    answer = answer,
                                    type = questionType,
                                    subject = null,
                                    isExclusive = isExclusive
                                )
                            )
                        }
                    }
                }
            }
        }
        
        // Sort by date (simple string sort works for most date formats)
        questions.sortBy { it.date }
        
        return ClassroomData(gameId = gameId, questions = questions)
    }
}
