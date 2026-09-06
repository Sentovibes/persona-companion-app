package com.persona.companion.wear.data

import android.content.Context
import android.util.Log
import com.persona.companion.wear.models.WearChoice
import com.persona.companion.wear.models.WearClassroomItem
import com.persona.companion.wear.models.WearDialogue
import com.persona.companion.wear.models.WearEnemyItem
import com.persona.companion.wear.models.WearSocialLink
import com.persona.companion.wear.models.WearSocialLinkRank
import org.json.JSONArray
import org.json.JSONObject

object WearDataLoader {
    private const val TAG = "WearDataLoader"

    // Elements by game series
    private val P3_ELEMENTS = listOf("Slash", "Strike", "Pierce", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", "Almighty")
    private val P4_ELEMENTS = listOf("Phys", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", "Almighty")
    private val P5_ELEMENTS = listOf("Phys", "Gun", "Fire", "Ice", "Elec", "Wind", "Psy", "Nuke", "Bless", "Curse")

    fun loadClassroom(context: Context, gameId: String): List<WearClassroomItem> {
        val filename = when (gameId) {
            "p3fes", "p3p", "p3r" -> "data/classroom/p3_classroom_answers.json"
            "p4", "p4g" -> "data/classroom/p4_classroom_answers.json"
            "p5", "p5r" -> "data/classroom/p5_classroom_answers.json"
            else -> return emptyList()
        }

        return try {
            val jsonString = context.assets.open(filename).bufferedReader().use { it.readText() }
            val rootJson = JSONObject(jsonString)
            val isRoyalOrGolden = gameId == "p5r" || gameId == "p4g"
            val list = mutableListOf<WearClassroomItem>()

            val months = rootJson.keys()
            while (months.hasNext()) {
                val month = months.next()
                val monthData = rootJson.getJSONObject(month)
                val types = monthData.keys()
                while (types.hasNext()) {
                    val typeStr = types.next()
                    val typeData = monthData.getJSONObject(typeStr)
                    val isExam = typeStr.contains("exam", ignoreCase = true) ||
                            typeStr.contains("midterm", ignoreCase = true) ||
                            typeStr.contains("final", ignoreCase = true)

                    val dates = typeData.keys()
                    while (dates.hasNext()) {
                        val date = dates.next()
                        val questionsArray = typeData.getJSONArray(date)
                        for (i in 0 until questionsArray.length()) {
                            val qObj = questionsArray.getJSONObject(i)
                            val isP5RExclusive = qObj.optBoolean("P5R Exclusive", false)
                            val isP4GExclusive = qObj.optBoolean("P4G Exclusive", false)
                            if ((isP5RExclusive || isP4GExclusive) && !isRoyalOrGolden) continue

                            val question = qObj.optString("Question", "")
                            val answer = qObj.optString("Answer", "")
                            if (answer.isNotEmpty()) {
                                list.add(
                                    WearClassroomItem(
                                        date = date,
                                        question = if (question.isNotEmpty()) question else "Question $date",
                                        answer = answer,
                                        isExam = isExam
                                    )
                                )
                            }
                        }
                    }
                }
            }

            list.sortWith(compareBy(
                {
                    val m = it.date.substringBefore("/").trim().toIntOrNull() ?: 0
                    if (m >= 4) m - 4 else m + 8
                },
                {
                    it.date.substringAfter("/").trim().toIntOrNull() ?: 0
                }
            ))
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error loading classroom for $gameId", e)
            emptyList()
        }
    }

    fun loadEnemies(context: Context, gameId: String): List<WearEnemyItem> {
        val filename = when (gameId) {
            "p3fes" -> "data/enemies/p3fes_enemies.json"
            "p3p" -> "data/enemies/p3p_enemies.json"
            "p3r" -> "data/enemies/p3r_enemies.json"
            "p4" -> "data/enemies/p4_enemies.json"
            "p4g" -> "data/enemies/p4g_enemies.json"
            "p5" -> "data/enemies/p5_enemies.json"
            "p5r" -> "data/enemies/p5r_enemies.json"
            else -> return emptyList()
        }

        val elements = when {
            gameId.startsWith("p3") -> P3_ELEMENTS
            gameId.startsWith("p4") -> P4_ELEMENTS
            gameId.startsWith("p5") -> P5_ELEMENTS
            else -> P4_ELEMENTS
        }

        return try {
            val jsonString = context.assets.open(filename).bufferedReader().use { it.readText() }
            val array = JSONArray(jsonString)
            val list = mutableListOf<WearEnemyItem>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val name = obj.optString("name", "")
                val arcana = obj.optString("arcana", "")
                val level = obj.optInt("level", 1)
                val resists = obj.optString("resists", "")
                val area = obj.optString("area", "")

                val isBoss = obj.optBoolean("isBoss", false) || area.equals("Boss", ignoreCase = true)
                val isMiniBoss = obj.optBoolean("isMiniBoss", false) || area.contains("Mini-Boss", ignoreCase = true)

                val weaknesses = mutableListOf<String>()
                val resistances = mutableListOf<Pair<String, String>>()

                resists.forEachIndexed { index, code ->
                    if (index < elements.size) {
                        val elem = elements[index]
                        val label = when (code) {
                            'w', 'W' -> {
                                weaknesses.add(elem)
                                "Weak"
                            }
                            'r', 's', 'S' -> "Resist"
                            'n', 'N', '_' -> "Null"
                            'p', 'P', 'R' -> "Repel"
                            'd', 'D', 'a', 'A' -> "Drain"
                            else -> ""
                        }
                        if (label.isNotEmpty()) {
                            resistances.add(elem to label)
                        }
                    }
                }

                if (name.isNotEmpty()) {
                    list.add(
                        WearEnemyItem(
                            name = name,
                            arcana = arcana,
                            level = level,
                            weaknesses = weaknesses,
                            resistances = resistances,
                            area = area,
                            isBoss = isBoss,
                            isMiniBoss = isMiniBoss
                        )
                    )
                }
            }

            list.sortedWith(compareBy({ it.level }, { it.name }))
        } catch (e: Exception) {
            Log.e(TAG, "Error loading enemies for $gameId", e)
            emptyList()
        }
    }

    fun loadSocialLinks(context: Context, gameId: String): List<WearSocialLink> {
        val filename = when (gameId) {
            "p3fes" -> "data/social-links/p3fes_social_links.json"
            "p3p" -> "data/social-links/p3p_male_social_links.json"
            "p3r" -> "data/social-links/p3r_social_links.json"
            "p4", "p4g" -> "data/social-links/p4+p4g_social_links.json"
            "p5", "p5r" -> "data/social-links/p5+p5r_social_links.json"
            else -> return emptyList()
        }

        return try {
            val jsonString = context.assets.open(filename).bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val list = mutableListOf<WearSocialLink>()

            val arcanaIter = root.keys()
            while (arcanaIter.hasNext()) {
                val arcana = arcanaIter.next()
                val arcanaData = root.getJSONObject(arcana)

                val isP4GExclusive = arcanaData.optBoolean("P4G Exclusive", false)
                val isP5RExclusive = arcanaData.optBoolean("P5R Exclusive", false)
                if (isP4GExclusive && gameId == "p4") continue
                if (isP5RExclusive && gameId == "p5") continue

                val ranks = mutableListOf<WearSocialLinkRank>()
                var rankCounter = 1

                // Top level auto ranks
                val topKeys = arcanaData.keys()
                while (topKeys.hasNext()) {
                    val key = topKeys.next()
                    if (key in listOf("P4G Exclusive", "P5R Exclusive", "P5R Reworked", "Details", "Rank Up Progression")) continue
                    if (key.contains("Auto", ignoreCase = true)) {
                        ranks.add(
                            WearSocialLinkRank(
                                rank = rankCounter++,
                                rankName = key,
                                dialogues = emptyList()
                            )
                        )
                    }
                }

                // Progression ranks
                arcanaData.optJSONObject("Rank Up Progression")?.let { prog ->
                    val rKeys = prog.keys()
                    while (rKeys.hasNext()) {
                        val rKey = rKeys.next()
                        val rObj = prog.optJSONObject(rKey) ?: continue
                        val dialoguesArray = rObj.optJSONArray("Dialogues")
                        val parsedDialogues = mutableListOf<WearDialogue>()

                        if (dialoguesArray != null) {
                            for (i in 0 until dialoguesArray.length()) {
                                val d = dialoguesArray.optJSONObject(i) ?: continue
                                val question = d.optString("Question", "")
                                val choicesArray = d.optJSONArray("Choices") ?: continue
                                val choices = mutableListOf<WearChoice>()

                                for (j in 0 until choicesArray.length()) {
                                    val c = choicesArray.optJSONObject(j) ?: continue
                                    val answer = c.optString("Answer", "")
                                    val points = c.optInt("Points", 0)
                                    val isPhone = question.contains("Phone", ignoreCase = true)
                                    if (answer.isNotEmpty()) {
                                        choices.add(WearChoice(text = answer, points = points, isPhone = isPhone))
                                    }
                                }

                                choices.sortByDescending { it.points }
                                if (choices.isNotEmpty()) {
                                    parsedDialogues.add(WearDialogue(question = question, bestChoices = choices))
                                }
                            }
                        }

                        val rankNum = rKey.filter { it.isDigit() }.toIntOrNull() ?: rankCounter++
                        ranks.add(
                            WearSocialLinkRank(
                                rank = rankNum,
                                rankName = rKey,
                                dialogues = parsedDialogues
                            )
                        )
                    }
                }

                ranks.sortBy { it.rank }

                val characterName = resolveCharacterName(gameId, arcana)

                list.add(
                    WearSocialLink(
                        arcana = arcana,
                        characterName = characterName,
                        ranks = ranks
                    )
                )
            }

            list.sortBy { it.arcana }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error loading social links for $gameId", e)
            emptyList()
        }
    }

    private fun resolveCharacterName(gameId: String, arcana: String): String {
        val clean = arcana.trim()
        val g = gameId.lowercase()
        return when {
            g.startsWith("p5") -> when (clean) {
                "Fool" -> "Igor"
                "Magician" -> "Morgana"
                "Priestess" -> "Makoto Niijima"
                "Empress" -> "Haru Okumura"
                "Emperor" -> "Yusuke Kitagawa"
                "Hierophant" -> "Sojiro Sakura"
                "Lovers" -> "Ann Takamaki"
                "Chariot" -> "Ryuji Sakamoto"
                "Justice" -> "Goro Akechi"
                "Hermit" -> "Futaba Sakura"
                "Fortune" -> "Chihaya Mifune"
                "Strength" -> "Caroline & Justine"
                "Hanged-Man", "Hanged Man" -> "Munehisa Iwai"
                "Death" -> "Tae Takemi"
                "Temperance" -> "Sadayo Kawakami"
                "Devil" -> "Ichiko Ohya"
                "Tower" -> "Shinya Oda"
                "Star" -> "Hifumi Togo"
                "Moon" -> "Yuuki Mishima"
                "Sun" -> "Toranosuke Yoshida"
                "Judgement" -> "Sae Niijima"
                "Faith" -> "Kasumi Yoshizawa"
                "Councillor" -> "Takuto Maruki"
                else -> clean
            }
            g.startsWith("p4") -> when {
                clean == "Fool" -> "Investigation Team"
                clean == "Magician" -> "Yosuke Hanamura"
                clean == "Priestess" -> "Yukiko Amagi"
                clean == "Empress" -> "Margaret"
                clean == "Emperor" -> "Kanji Tatsumi"
                clean == "Hierophant" -> "Ryotaro Dojima"
                clean == "Lovers" -> "Rise Kujikawa"
                clean == "Chariot" -> "Chie Satonaka"
                clean == "Justice" -> "Nanako Dojima"
                clean == "Hermit" -> "Fox"
                clean == "Fortune" -> "Naoto Shirogane"
                clean.startsWith("Strength") -> "Kou / Daisuke"
                clean.startsWith("Hanged") -> "Naoki Konishi"
                clean == "Death" -> "Hisano Kuroda"
                clean == "Temperance" -> "Eri Minami"
                clean == "Devil" -> "Sayoko Uehara"
                clean == "Tower" -> "Shu Nakajima"
                clean == "Star" -> "Teddie"
                clean == "Moon" -> "Ai Ebihara"
                clean.startsWith("Sun") -> "Yumi / Ayane"
                clean == "Judgement" -> "Seekers of Truth"
                clean in listOf("Jester", "Hunger") -> "Tohru Adachi"
                clean == "Aeon" -> "Marie"
                else -> clean
            }
            g.startsWith("p3") -> when (clean) {
                "Fool" -> "SEES"
                "Magician" -> "Kenji Tomochika"
                "Priestess" -> "Fuuka Yamagishi"
                "Empress" -> "Mitsuru Kirijo"
                "Emperor" -> "Hidetoshi Odagiri"
                "Hierophant" -> "Bunkichi & Mitsuko"
                "Lovers" -> "Yukari Takeba"
                "Chariot" -> "Kazushi Miyamoto"
                "Justice" -> "Chihiro Fushimi"
                "Hermit" -> "Maya"
                "Fortune" -> "Keisuke Hiraga"
                "Strength" -> "Yuko Nishiwaki"
                "Hanged-Man", "Hanged Man" -> "Maiko Oohashi"
                "Death" -> "Pharos"
                "Temperance" -> "Bebe"
                "Devil" -> "President Tanaka"
                "Tower" -> "Mutatsu"
                "Star" -> "Mamoru Hayase"
                "Moon" -> "Nozomi Suemitsu"
                "Sun" -> "Akinari Kamiki"
                "Judgement" -> "Nyx Annihilation Team"
                "Aeon" -> "Aigis"
                else -> clean
            }
            else -> clean
        }
    }
}
