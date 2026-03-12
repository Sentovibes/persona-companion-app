package com.persona.companion.data

import android.content.Context
import android.util.Log
import com.persona.companion.models.DialogueChoice
import com.persona.companion.models.SocialLink
import com.persona.companion.models.SocialLinkRank
import com.persona.companion.models.SocialLinksData
import org.json.JSONObject

object SocialLinkLoader {
    private const val TAG = "SocialLinkLoader"
    
    /**
     * Load Social Links data for a specific game
     */
    fun loadSocialLinks(context: Context, gameId: String): SocialLinksData? {
        return try {
            Log.d(TAG, "Loading social links for gameId: $gameId")
            val filename = getSocialLinkFilename(context, gameId)
            if (filename == null) {
                Log.e(TAG, "No filename found for gameId: $gameId")
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
            
            val result = parseSocialLinksJson(gameId, jsonString)
            Log.d(TAG, "Parsed ${result.socialLinks.size} social links")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error loading social links for $gameId", e)
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get the filename for a game's social links data
     * For P3P, checks user preference for Male MC or FeMC
     * For P4/P4G, uses the same combined file
     * For P5/P5R, uses the same combined file
     */
    private fun getSocialLinkFilename(context: Context, gameId: String): String? {
        return when (gameId) {
            "p3fes" -> "data/social-links/p3fes_social_links.json"
            "p3p" -> {
                // Check user preference for P3P protagonist
                val prefs = UserPreferences(context)
                val protagonist = prefs.getP3PProtagonist()
                when (protagonist) {
                    UserPreferences.P3PProtagonist.MALE -> "data/social-links/p3p_male_social_links.json"
                    UserPreferences.P3PProtagonist.FEMC -> "data/social-links/p3p_femc_social_links.json"
                }
            }
            "p3r" -> "data/social-links/p3r_social_links.json"
            "p4", "p4g" -> "data/social-links/p4+p4g_social_links.json"  // Shared file
            "p5", "p5r" -> "data/social-links/p5+p5r_social_links.json"  // Shared file
            else -> {
                Log.w(TAG, "No Social Links data available for gameId: $gameId")
                null
            }
        }
    }
    
    /**
     * Parse Social Links JSON data
     */
    private fun parseSocialLinksJson(gameId: String, jsonString: String): SocialLinksData {
        val socialLinks = mutableListOf<SocialLink>()
        val rootJson = JSONObject(jsonString)
        
        // Iterate through each arcana
        val arcanaNames = rootJson.keys()
        while (arcanaNames.hasNext()) {
            val arcana = arcanaNames.next()
            val arcanaData = rootJson.getJSONObject(arcana)
            
            // Check if this is P4G or P5R exclusive
            val isP4GExclusive = arcanaData.optBoolean("P4G Exclusive", false)
            val isP5RExclusive = arcanaData.optBoolean("P5R Exclusive", false)
            
            // Skip exclusive Social Links if playing base game
            if (isP4GExclusive && gameId == "p4") {
                continue
            }
            if (isP5RExclusive && gameId == "p5") {
                continue
            }
            
            val ranks = mutableListOf<SocialLinkRank>()
            
            // Iterate through each rank
            val rankKeys = arcanaData.keys()
            var rankNumber = 1
            while (rankKeys.hasNext()) {
                val rankKey = rankKeys.next()
                
                // Skip the exclusive flags and prerequisite flags
                if (rankKey == "P4G Exclusive" || 
                    rankKey == "P5R Exclusive" || 
                    rankKey == "P5R Reworked" ||
                    rankKey.startsWith("Flag")) {
                    continue
                }
                
                // Try to get rank data, skip if it's not a JSONObject
                val rankData = try {
                    arcanaData.getJSONObject(rankKey)
                } catch (e: Exception) {
                    Log.w(TAG, "Skipping non-object key: $rankKey in $arcana")
                    continue
                }
                
                // Parse rank name and check if auto
                val isAuto = rankKey.contains("Auto", ignoreCase = true)
                
                // Get next rank points
                val nextRankPoints = rankData.optInt("Next Rank", 0)
                
                // Get requirements
                val requirements = extractRequirements(rankData)
                
                // Parse dialogue choices
                val choices = parseDialogueChoices(rankData)
                
                ranks.add(
                    SocialLinkRank(
                        rankNumber = rankNumber,
                        rankName = rankKey,
                        isAuto = isAuto,
                        nextRankPoints = nextRankPoints,
                        requirements = requirements,
                        choices = choices
                    )
                )
                rankNumber++
            }
            
            socialLinks.add(
                SocialLink(
                    arcana = arcana,
                    ranks = ranks,
                    isP4GExclusive = isP4GExclusive,
                    isP5RExclusive = isP5RExclusive
                )
            )
        }
        
        return SocialLinksData(gameId = gameId, socialLinks = socialLinks)
    }
    
    /**
     * Extract requirements from rank data
     */
    private fun extractRequirements(rankData: JSONObject): String? {
        val requirements = mutableListOf<String>()
        
        // Check for various requirement fields
        rankData.keys().forEach { key ->
            when {
                key.contains("Requires", ignoreCase = true) -> {
                    val value = rankData.optString(key)
                    if (value.isNotEmpty() && value != "0") {
                        requirements.add(value)
                    }
                }
                key.contains("Courage", ignoreCase = true) ||
                key.contains("Knowledge", ignoreCase = true) ||
                key.contains("Charm", ignoreCase = true) ||
                key.contains("Diligence", ignoreCase = true) ||
                key.contains("Understanding", ignoreCase = true) -> {
                    val value = rankData.optString(key)
                    if (value.isNotEmpty() && value != "0") {
                        requirements.add("$key: $value")
                    }
                }
            }
        }
        
        return if (requirements.isNotEmpty()) requirements.joinToString(", ") else null
    }
    
    /**
     * Parse dialogue choices from rank data
     */
    private fun parseDialogueChoices(rankData: JSONObject): List<DialogueChoice> {
        val choices = mutableListOf<DialogueChoice>()
        
        rankData.keys().forEach { key ->
            val value = rankData.opt(key)
            
            // Skip non-choice fields
            if (key in listOf("Next Rank", "Requires", "Requirements")) {
                return@forEach
            }
            
            // Skip requirement-related fields
            if (key.contains("Courage", ignoreCase = true) ||
                key.contains("Knowledge", ignoreCase = true) ||
                key.contains("Charm", ignoreCase = true) ||
                key.contains("Diligence", ignoreCase = true) ||
                key.contains("Understanding", ignoreCase = true) ||
                key.contains("Guts", ignoreCase = true) ||
                key.contains("Proficiency", ignoreCase = true) ||
                key.contains("Kindness", ignoreCase = true) ||
                key.contains("Lv.", ignoreCase = true) ||
                key.contains("Level", ignoreCase = true)) {
                return@forEach
            }
            
            // Skip dates and weather conditions
            if (key.matches(Regex("^\\d{1,2}/\\d{1,2}$")) ||  // Dates like "4/15" or "12/24"
                key.contains("raining", ignoreCase = true) ||
                key.contains("Not raining", ignoreCase = true) ||
                key.contains("Palace", ignoreCase = true) ||
                key.contains("completion", ignoreCase = true) ||
                key.contains("Unlocks", ignoreCase = true)) {
                return@forEach
            }
            
            // Check if this is a dialogue choice
            when {
                // Regular choice: "1: Choice text", "1. Choice text", "Q1: Choice text"
                key.matches(Regex("^[QA]?[0-9]+[:.] .*")) -> {
                    val points = when (value) {
                        is Number -> value.toInt()
                        is String -> value.toIntOrNull() ?: 0
                        else -> 0
                    }
                    choices.add(DialogueChoice(text = key, points = points, isPhoneChoice = false))
                }
                // Phone choice: "Phone 1: Choice text" or "Phone 1. Choice text"
                key.startsWith("Phone", ignoreCase = true) -> {
                    val points = when (value) {
                        is Number -> value.toInt()
                        is String -> value.toIntOrNull() ?: 0
                        else -> 0
                    }
                    choices.add(DialogueChoice(text = key, points = points, isPhoneChoice = true))
                }
                // "Any" choice or "Any_1", "Any_2", etc.
                key.startsWith("Any", ignoreCase = true) -> {
                    val points = when (value) {
                        is Number -> value.toInt()
                        is String -> value.toIntOrNull() ?: 0
                        else -> 0
                    }
                    choices.add(DialogueChoice(text = "Any choice", points = points, isPhoneChoice = false))
                }
                // Catch-all for other dialogue choices (text with points > 0)
                value is Number && value.toInt() > 0 -> {
                    choices.add(DialogueChoice(text = key, points = value.toInt(), isPhoneChoice = false))
                }
            }
        }
        
        return choices
    }
}
