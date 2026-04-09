package com.persona.companion.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.data.repositories.SkillRepository
import com.persona.companion.data.PersonaRepository
import com.persona.companion.models.Skill
import com.persona.companion.models.Persona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SkillViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SkillRepository(application)
    private val personaRepository = PersonaRepository(application)

    private val _skills = MutableStateFlow<List<Skill>>(emptyList())
    val skills: StateFlow<List<Skill>> = _skills

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadSkills(skillPath: String?) {
        if (skillPath == null) {
            _skills.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _skills.value = repository.getSkills(skillPath)
            _isLoading.value = false
        }
    }

    fun getLearners(personaPath: String?, skillName: String): List<Persona> {
        if (personaPath == null) return emptyList()
        return personaRepository.getPersonasBySkill(personaPath, skillName)
    }
}
