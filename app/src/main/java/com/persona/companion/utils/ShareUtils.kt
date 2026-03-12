package com.persona.companion.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.persona.companion.models.Enemy
import com.persona.companion.models.Persona
import java.io.File
import java.io.FileOutputStream

object ShareUtils {
    
    fun sharePersona(context: Context, persona: Persona, gameName: String, gameId: String? = null) {
        val text = buildString {
            appendLine("Persona: ${persona.name}")
            appendLine("Game: $gameName")
            appendLine("Arcana: ${persona.arcana ?: "Unknown"}")
            appendLine("Level: ${persona.level}")
            
            if (persona.stats != null && persona.stats.size >= 5) {
                appendLine()
                appendLine("Base Stats:")
                appendLine("  Strength: ${persona.stats[0]}")
                appendLine("  Magic: ${persona.stats[1]}")
                appendLine("  Endurance: ${persona.stats[2]}")
                appendLine("  Agility: ${persona.stats[3]}")
                appendLine("  Luck: ${persona.stats[4]}")
            }
            
            if (!persona.skills.isNullOrEmpty()) {
                appendLine()
                appendLine("Skills:")
                persona.skills.entries.forEach { (skill, level) ->
                    val levelText = when {
                        level < 1.0 -> "Innate"
                        level >= 100 -> "Special"
                        else -> "Lv. ${level.toInt()}"
                    }
                    appendLine("  - $skill ($levelText)")
                }
            }
            
            if (persona.weaknesses.isNotEmpty() || persona.resistances.isNotEmpty() || 
                persona.nullifies.isNotEmpty() || persona.repels.isNotEmpty() || 
                persona.absorbs.isNotEmpty()) {
                appendLine()
                appendLine("Elemental Affinities:")
                if (persona.weaknesses.isNotEmpty()) {
                    appendLine("  Weak: ${persona.weaknesses.joinToString(", ")}")
                }
                if (persona.resistances.isNotEmpty()) {
                    appendLine("  Resist: ${persona.resistances.joinToString(", ")}")
                }
                if (persona.nullifies.isNotEmpty()) {
                    appendLine("  Null: ${persona.nullifies.joinToString(", ")}")
                }
                if (persona.repels.isNotEmpty()) {
                    appendLine("  Repel: ${persona.repels.joinToString(", ")}")
                }
                if (persona.absorbs.isNotEmpty()) {
                    appendLine("  Absorb: ${persona.absorbs.joinToString(", ")}")
                }
            }
            
            if (!persona.trait.isNullOrBlank()) {
                appendLine()
                appendLine("Trait: ${persona.trait}")
            }
            
            if (!persona.item.isNullOrBlank()) {
                appendLine("Item: ${persona.item}")
            }
            
            appendLine()
            appendLine("---")
            appendLine("Shared from Persona Companion")
        }
        
        // Try to load and share image if available
        val imagePath = ImageUtils.getImagePath(persona.name, isEnemy = false, gameId = gameId)
        val bitmap = ImageUtils.loadImageFromAssets(context, imagePath)
        
        if (bitmap != null) {
            shareTextWithImage(context, text, bitmap, "Share Persona")
        } else {
            shareText(context, text, "Share Persona")
        }
    }
    
    fun shareEnemy(context: Context, enemy: Enemy, gameName: String, gameId: String? = null) {
        val text = buildString {
            appendLine("Enemy: ${enemy.name}")
            appendLine("Game: $gameName")
            appendLine("Arcana: ${enemy.arcana}")
            appendLine("Level: ${enemy.level}")
            appendLine("HP: ${enemy.hp}")
            appendLine("SP: ${enemy.sp}")
            
            if (!enemy.version.isNullOrEmpty()) {
                appendLine("Version: ${enemy.version}")
            }
            
            if (enemy.stats != null) {
                appendLine()
                appendLine("Stats:")
                appendLine("  Strength: ${enemy.stats.strength}")
                appendLine("  Magic: ${enemy.stats.magic}")
                appendLine("  Endurance: ${enemy.stats.endurance}")
                appendLine("  Agility: ${enemy.stats.agility}")
                appendLine("  Luck: ${enemy.stats.luck}")
            }
            
            if (enemy.skills.isNotEmpty()) {
                appendLine()
                appendLine("Skills:")
                enemy.skills.forEach { skill ->
                    appendLine("  - $skill")
                }
            }
            
            if (enemy.resists.isNotBlank() && enemy.resists != "-") {
                val formattedResists = ResistanceUtils.formatResistancesWithIcons(enemy.resists, gameId ?: "")
                if (formattedResists.isNotEmpty()) {
                    appendLine()
                    appendLine("Resistances:")
                    appendLine("  $formattedResists")
                }
            }
            
            // Boss Phases
            if (!enemy.phases.isNullOrEmpty()) {
                appendLine()
                appendLine("━━━━━━━━━━━━━━━━━━━━")
                appendLine("BOSS PHASES")
                appendLine("━━━━━━━━━━━━━━━━━━━━")
                enemy.phases.forEach { phase ->
                    appendLine()
                    appendLine("Phase: ${phase.name}")
                    appendLine("  HP: ${phase.hp}")
                    appendLine("  SP: ${phase.sp}")
                    
                    val phaseResists = ResistanceUtils.formatResistancesWithIcons(phase.resists, gameId ?: "")
                    if (phaseResists.isNotEmpty()) {
                        appendLine("  Resistances: $phaseResists")
                    }
                    
                    if (phase.skills.isNotEmpty()) {
                        appendLine("  Skills:")
                        phase.skills.forEach { skill ->
                            appendLine("    - $skill")
                        }
                    }
                    
                    // Parts within phase
                    if (!phase.parts.isNullOrEmpty()) {
                        appendLine("  Parts:")
                        phase.parts.forEach { part ->
                            appendLine("    • ${part.name} (HP: ${part.hp})")
                            val partResists = ResistanceUtils.formatResistancesWithIcons(part.resists, gameId ?: "")
                            if (partResists.isNotEmpty()) {
                                appendLine("      Resistances: $partResists")
                            }
                            if (!part.skills.isNullOrEmpty()) {
                                part.skills.forEach { skill ->
                                    appendLine("      - $skill")
                                }
                            }
                        }
                    }
                }
            }
            
            // Boss Parts (without phases)
            if (!enemy.parts.isNullOrEmpty() && enemy.phases.isNullOrEmpty()) {
                appendLine()
                appendLine("━━━━━━━━━━━━━━━━━━━━")
                appendLine("BOSS PARTS")
                appendLine("━━━━━━━━━━━━━━━━━━━━")
                enemy.parts.forEach { part ->
                    appendLine()
                    appendLine("Part: ${part.name}")
                    appendLine("  HP: ${part.hp}")
                    if (part.sp != null) {
                        appendLine("  SP: ${part.sp}")
                    }
                    
                    val partResists = ResistanceUtils.formatResistancesWithIcons(part.resists, gameId ?: "")
                    if (partResists.isNotEmpty()) {
                        appendLine("  Resistances: $partResists")
                    }
                    
                    if (!part.skills.isNullOrEmpty()) {
                        appendLine("  Skills:")
                        part.skills.forEach { skill ->
                            appendLine("    - $skill")
                        }
                    }
                }
            }
            
            if (enemy.area.isNotEmpty() && enemy.area != "Unknown") {
                appendLine()
                appendLine("Location: ${enemy.area}")
            }
            
            if (enemy.drops != null) {
                appendLine()
                appendLine("Drops:")
                if (enemy.drops.gem != "-" && enemy.drops.gem.isNotBlank()) {
                    appendLine("  Gem: ${enemy.drops.gem}")
                }
                if (enemy.drops.item != "-" && enemy.drops.item.isNotBlank()) {
                    appendLine("  Item: ${enemy.drops.item}")
                }
            }
            
            if (enemy.exp > 0) {
                appendLine()
                appendLine("Rewards:")
                appendLine("  EXP: ${enemy.exp}")
            }
            
            appendLine()
            appendLine("---")
            appendLine("Shared from Persona Companion")
        }
        
        // Try to load and share image if available
        val imagePath = ImageUtils.getImagePath(enemy.name, isEnemy = true, gameId = gameId)
        val bitmap = ImageUtils.loadImageFromAssets(context, imagePath)
        
        if (bitmap != null) {
            shareTextWithImage(context, text, bitmap, "Share Enemy")
        } else {
            shareText(context, text, "Share Enemy")
        }
    }
    
    private fun shareText(context: Context, text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
    
    private fun shareTextWithImage(context: Context, text: String, bitmap: Bitmap, title: String) {
        try {
            // Save bitmap to cache
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "share_image.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            
            // Get URI using FileProvider
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            // Share with image
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            // Fallback to text only if image sharing fails
            shareText(context, text, title)
        }
    }
    
    fun shareSocialLink(context: Context, socialLink: com.persona.companion.models.SocialLink, gameName: String) {
        val text = buildString {
            appendLine("Social Link: ${socialLink.arcana}")
            appendLine("Game: $gameName")
            appendLine()
            
            socialLink.ranks.forEach { rank ->
                appendLine("━━━ ${rank.rankName} ━━━")
                
                if (rank.isAuto) {
                    appendLine("(Automatic)")
                }
                
                if (rank.requirements != null) {
                    appendLine("Requirements: ${rank.requirements}")
                }
                
                if (rank.nextRankPoints > 0) {
                    appendLine("Points needed: ${rank.nextRankPoints}")
                }
                
                if (rank.location != null) {
                    appendLine("Location: ${rank.location}")
                }
                
                if (rank.availability != null) {
                    appendLine("Availability: ${rank.availability}")
                }
                
                if (rank.choices.isNotEmpty()) {
                    appendLine()
                    appendLine("Dialogue Choices:")
                    rank.choices.forEach { choice ->
                        val phonePrefix = if (choice.isPhoneChoice) "📱 " else ""
                        val pointsText = if (choice.points > 0) "+${choice.points}" else "${choice.points}"
                        appendLine("  $phonePrefix${choice.text} ($pointsText pts)")
                    }
                }
                
                appendLine()
            }
            
            appendLine("---")
            appendLine("Shared from Persona Companion")
        }
        
        shareText(context, text, "Share Social Link")
    }
}
