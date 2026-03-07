package com.persona.companion.models

data class FusionChart(
    val races: List<String>,
    val table: List<List<String>>
) {
    fun getFusionResult(arcana1: String, arcana2: String): String? {
        val index1 = races.indexOf(arcana1)
        val index2 = races.indexOf(arcana2)
        
        if (index1 < 0 || index2 < 0) return null
        
        // Fusion chart is symmetric, so we need to handle both directions
        val result = if (index1 <= index2) {
            table.getOrNull(index2)?.getOrNull(index1)
        } else {
            table.getOrNull(index1)?.getOrNull(index2)
        }
        
        return if (result == "-" || result.isNullOrEmpty()) null else result
    }
}
