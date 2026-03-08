package com.persona.companion.utils

import com.persona.companion.models.*

object FilterUtils {
    
    fun filterAndSortPersonas(
        personas: List<Persona>,
        filters: PersonaFilters,
        favorites: Set<String>
    ): List<Persona> {
        var filtered = personas
        
        // Level range
        filtered = filtered.filter { (it.level ?: 0) in filters.minLevel..filters.maxLevel }
        
        // Skill type
        if (filters.skillType != SkillType.ALL) {
            filtered = filtered.filter { persona ->
                persona.skills?.keys?.any { skill ->
                    matchesSkillType(skill, filters.skillType)
                } == true
            }
        }
        
        // Game exclusive (has DLC or special flags)
        if (filters.gameExclusive) {
            filtered = filtered.filter { it.dlc != null && it.dlc > 0 }
        }
        
        // DLC only
        if (filters.dlcOnly) {
            filtered = filtered.filter { it.isDlc == true || (it.dlc != null && it.dlc > 0) }
        }
        
        // Favorites only
        if (filters.showFavoritesOnly) {
            filtered = filtered.filter { favorites.contains(getPersonaId(it)) }
        }
        
        // Sort
        filtered = when (filters.sortOption) {
            PersonaSortOption.LEVEL_ASC -> filtered.sortedBy { it.level ?: 0 }
            PersonaSortOption.LEVEL_DESC -> filtered.sortedByDescending { it.level ?: 0 }
            PersonaSortOption.NAME_ASC -> filtered.sortedBy { it.name }
            PersonaSortOption.NAME_DESC -> filtered.sortedByDescending { it.name }
            PersonaSortOption.ARCANA -> filtered.sortedBy { it.arcana ?: "Unknown" }
            PersonaSortOption.STRENGTH -> filtered.sortedByDescending { 
                it.strength ?: it.stats?.getOrNull(2) ?: 0
            }
            PersonaSortOption.MAGIC -> filtered.sortedByDescending { 
                it.magic ?: it.stats?.getOrNull(3) ?: 0
            }
            PersonaSortOption.ENDURANCE -> filtered.sortedByDescending { 
                it.endurance ?: it.stats?.getOrNull(4) ?: 0
            }
            PersonaSortOption.AGILITY -> filtered.sortedByDescending { 
                it.agility ?: it.stats?.getOrNull(5) ?: 0
            }
            PersonaSortOption.LUCK -> filtered.sortedByDescending { 
                it.luck ?: it.stats?.getOrNull(6) ?: 0
            }
        }
        
        return filtered
    }
    
    fun filterAndSortEnemies(
        enemies: List<Enemy>,
        filters: EnemyFilters,
        favorites: Set<String>,
        elements: List<String>
    ): List<Enemy> {
        var filtered = enemies
        
        // Level range
        filtered = filtered.filter { it.level in filters.minLevel..filters.maxLevel }
        
        // Resistance filter
        if (filters.resistanceType != null) {
            val elementIndex = elements.indexOf(filters.resistanceType)
            if (elementIndex >= 0) {
                filtered = filtered.filter { enemy ->
                    if (elementIndex < enemy.resists.length) {
                        val resist = enemy.resists[elementIndex]
                        when (filters.resistanceFilter) {
                            ResistanceFilter.ALL -> true
                            ResistanceFilter.WEAK -> resist == 'w'
                            ResistanceFilter.RESIST -> resist == 'r'
                            ResistanceFilter.NULL -> resist == 'n'
                            ResistanceFilter.DRAIN -> resist == 'd'
                        }
                    } else false
                }
            }
        }
        
        // Game exclusive
        if (filters.gameExclusive) {
            filtered = filtered.filter { 
                it.version?.contains("Exclusive", ignoreCase = true) == true ||
                it.version?.contains("DLC", ignoreCase = true) == true
            }
        }
        
        // Favorites only
        if (filters.showFavoritesOnly) {
            filtered = filtered.filter { favorites.contains(getEnemyId(it)) }
        }
        
        // Sort
        filtered = when (filters.sortOption) {
            EnemySortOption.LEVEL_ASC -> filtered.sortedBy { it.level }
            EnemySortOption.LEVEL_DESC -> filtered.sortedByDescending { it.level }
            EnemySortOption.NAME_ASC -> filtered.sortedBy { it.name }
            EnemySortOption.NAME_DESC -> filtered.sortedByDescending { it.name }
            EnemySortOption.HP_ASC -> filtered.sortedBy { it.hp }
            EnemySortOption.HP_DESC -> filtered.sortedByDescending { it.hp }
        }
        
        return filtered
    }
    
    private fun matchesSkillType(skillName: String, type: SkillType): Boolean {
        val lowerSkill = skillName.lowercase()
        return when (type) {
            SkillType.ALL -> true
            SkillType.PHYSICAL -> lowerSkill.contains("strike") || lowerSkill.contains("slash") || 
                                  lowerSkill.contains("pierce") || lowerSkill.contains("attack")
            SkillType.MAGIC -> lowerSkill.contains("agi") || lowerSkill.contains("bufu") || 
                              lowerSkill.contains("zio") || lowerSkill.contains("garu") ||
                              lowerSkill.contains("psy") || lowerSkill.contains("frei")
            SkillType.HEALING -> lowerSkill.contains("dia") || lowerSkill.contains("recarm") || 
                                lowerSkill.contains("heal")
            SkillType.SUPPORT -> lowerSkill.contains("kaja") || lowerSkill.contains("kunda") || 
                                lowerSkill.contains("wall") || lowerSkill.contains("break")
            SkillType.PASSIVE -> lowerSkill.contains("boost") || lowerSkill.contains("amp") || 
                                lowerSkill.contains("counter") || lowerSkill.contains("evade")
            SkillType.ALMIGHTY -> lowerSkill.contains("megido") || lowerSkill.contains("almighty")
        }
    }
    
    fun getPersonaId(persona: Persona): String = "${persona.name}_${persona.arcana}_${persona.level}"
    
    fun getEnemyId(enemy: Enemy): String = "${enemy.name}_${enemy.level}"
}
