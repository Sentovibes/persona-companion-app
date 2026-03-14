package com.persona.companion.data

import android.content.Context
import android.util.Log
import com.persona.companion.models.DialogueChoice
import com.persona.companion.models.SocialLink
import com.persona.companion.models.SocialLinkDetails
import com.persona.companion.models.SocialLinkDialogue
import com.persona.companion.models.SocialLinkRank
import com.persona.companion.models.SocialLinksData
import org.json.JSONArray
import org.json.JSONObject

object SocialLinkLoader {
    private const val TAG = "SocialLinkLoader"

    fun loadSocialLinks(context: Context, gameId: String): SocialLinksData? {
        return try {
            val filename = getSocialLinkFilename(context, gameId) ?: return null
            val jsonString = context.assets.open(filename).bufferedReader().use { it.readText() }
            parseSocialLinksJson(gameId, jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading social links for $gameId", e)
            null
        }
    }

    private fun getSocialLinkFilename(context: Context, gameId: String): String? {
        return when (gameId) {
            "p3fes" -> "data/social-links/p3fes_social_links.json"
            "p3p" -> {
                val prefs = UserPreferences(context)
                when (prefs.getP3PProtagonist()) {
                    UserPreferences.P3PProtagonist.MALE -> "data/social-links/p3p_male_social_links.json"
                    UserPreferences.P3PProtagonist.FEMC -> "data/social-links/p3p_femc_social_links.json"
                }
            }
            "p3r"       -> "data/social-links/p3r_social_links.json"
            "p4", "p4g" -> "data/social-links/p4+p4g_social_links.json"
            "p5", "p5r" -> "data/social-links/p5+p5r_social_links.json"
            else -> null
        }
    }

    private fun parseSocialLinksJson(gameId: String, jsonString: String): SocialLinksData {
        val socialLinks = mutableListOf<SocialLink>()
        val root = JSONObject(jsonString)

        val arcanaIter = root.keys()
        while (arcanaIter.hasNext()) {
            val arcana = arcanaIter.next()
            val arcanaData = root.getJSONObject(arcana)

            val isP4GExclusive = arcanaData.optBoolean("P4G Exclusive", false)
            val isP5RExclusive = arcanaData.optBoolean("P5R Exclusive", false)
            if (isP4GExclusive && gameId == "p4") continue
            if (isP5RExclusive && gameId == "p5") continue

            // Parse Details block
            val details = arcanaData.optJSONObject("Details")?.let { d ->
                SocialLinkDetails(
                    trigger    = d.optString("Trigger").takeIf { it.isNotEmpty() },
                    schedule   = d.optString("Schedule").takeIf { it.isNotEmpty() },
                    timeOfDay  = d.optString("Time of Day").takeIf { it.isNotEmpty() },
                    location   = d.optString("Location").takeIf { it.isNotEmpty() }
                )
            }

            val ranks = mutableListOf<SocialLinkRank>()
            var rankNumber = 1

            // Auto ranks at top level: "Rank N: Auto": { "Requirements": "..." }
            val topKeys = arcanaData.keys()
            while (topKeys.hasNext()) {
                val key = topKeys.next()
                if (key in listOf("P4G Exclusive", "P5R Exclusive", "P5R Reworked", "Details", "Rank Up Progression")) continue
                val rankData = arcanaData.optJSONObject(key) ?: continue
                ranks.add(
                    SocialLinkRank(
                        rankNumber    = rankNumber++,
                        rankName      = key,
                        isAuto        = key.contains("Auto", ignoreCase = true),
                        requirements  = rankData.optString("Requirements").takeIf { it.isNotEmpty() }
                    )
                )
            }

            // Rank Up Progression block
            arcanaData.optJSONObject("Rank Up Progression")?.let { progression ->
                val rankIter = progression.keys()
                while (rankIter.hasNext()) {
                    val rankKey = rankIter.next()
                    val rankData = progression.optJSONObject(rankKey) ?: continue

                    val nextPts      = rankData.optInt("Next Pts", 0)
                    val requirements = rankData.optString("Requirements").takeIf { it.isNotEmpty() }
                    val dialogues    = parseDialogues(rankData.optJSONArray("Dialogues"))

                    ranks.add(
                        SocialLinkRank(
                            rankNumber    = rankNumber++,
                            rankName      = rankKey,
                            isAuto        = false,
                            nextRankPoints = nextPts,
                            requirements  = requirements,
                            dialogues     = dialogues
                        )
                    )
                }
            }

            socialLinks.add(
                SocialLink(
                    arcana         = arcana,
                    ranks          = ranks,
                    details        = details,
                    isP4GExclusive = isP4GExclusive,
                    isP5RExclusive = isP5RExclusive
                )
            )
        }

        return SocialLinksData(gameId = gameId, socialLinks = socialLinks)
    }

    private fun parseDialogues(dialoguesArray: JSONArray?): List<SocialLinkDialogue> {
        if (dialoguesArray == null) return emptyList()
        val result = mutableListOf<SocialLinkDialogue>()
        for (i in 0 until dialoguesArray.length()) {
            val d = dialoguesArray.optJSONObject(i) ?: continue
            val question = d.optString("Question", "")
            val choicesArray = d.optJSONArray("Choices") ?: continue
            val choices = mutableListOf<DialogueChoice>()
            for (j in 0 until choicesArray.length()) {
                val c = choicesArray.optJSONObject(j) ?: continue
                val answer = c.optString("Answer", "")
                val points = c.optInt("Points", 0)
                val isPhone = question.contains("Phone", ignoreCase = true)
                choices.add(DialogueChoice(text = answer, points = points, isPhoneChoice = isPhone))
            }
            if (choices.isNotEmpty()) result.add(SocialLinkDialogue(question = question, choices = choices))
        }
        return result
    }
}
