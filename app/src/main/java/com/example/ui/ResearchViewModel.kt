package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiResearchRepository
import com.example.data.AiResearchRepositoryImpl
import com.example.data.DraftHistoryRepository
import com.example.model.ResearchPaperDraft
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ResearchUiState {
    data object Idle : ResearchUiState
    data class Generating(val step: String, val progress: Float) : ResearchUiState
    data class Success(val draft: ResearchPaperDraft) : ResearchUiState
    data class Error(val message: String) : ResearchUiState
}

enum class PaperSectionTab(val label: String) {
    FULL_PAPER("Full Paper"),
    ABSTRACT("Abstract"),
    INTRODUCTION("Introduction"),
    LITERATURE_REVIEW("Lit Review"),
    OBJECTIVES("Objectives"),
    METHODOLOGY("Methodology"),
    CONCLUSION("Conclusion")
}

class ResearchViewModel(
    private val repository: AiResearchRepository = AiResearchRepositoryImpl(),
    private val historyRepository: DraftHistoryRepository = DraftHistoryRepository()
) : ViewModel() {

    private val _topicInput = MutableStateFlow("")
    val topicInput: StateFlow<String> = _topicInput.asStateFlow()

    private val _selectedAcademicLevel = MutableStateFlow("Undergraduate")
    val selectedAcademicLevel: StateFlow<String> = _selectedAcademicLevel.asStateFlow()

    private val _selectedDiscipline = MutableStateFlow("Computer Science & AI")
    val selectedDiscipline: StateFlow<String> = _selectedDiscipline.asStateFlow()

    private val _uiState = MutableStateFlow<ResearchUiState>(ResearchUiState.Idle)
    val uiState: StateFlow<ResearchUiState> = _uiState.asStateFlow()

    val savedDrafts: StateFlow<List<ResearchPaperDraft>> = historyRepository.drafts

    private val _selectedTab = MutableStateFlow(PaperSectionTab.FULL_PAPER)
    val selectedTab: StateFlow<PaperSectionTab> = _selectedTab.asStateFlow()

    private val _showHistorySheet = MutableStateFlow(false)
    val showHistorySheet: StateFlow<Boolean> = _showHistorySheet.asStateFlow()

    init {
        // Pre-populate with Example 1 in history so student immediately has sample access
        viewModelScope.launch {
            val sample = repository.generatePaperDraft(
                topic = "Artificial Intelligence in Education",
                academicLevel = "Undergraduate",
                discipline = "Computer Science & AI"
            ).getOrNull()
            sample?.let { historyRepository.saveDraft(it) }
        }
    }

    fun onTopicChange(newTopic: String) {
        _topicInput.value = newTopic
    }

    fun onAcademicLevelSelect(level: String) {
        _selectedAcademicLevel.value = level
    }

    fun onDisciplineSelect(discipline: String) {
        _selectedDiscipline.value = discipline
    }

    fun onTabSelect(tab: PaperSectionTab) {
        _selectedTab.value = tab
    }

    fun toggleHistorySheet(show: Boolean) {
        _showHistorySheet.value = show
    }

    fun selectExamplePrompt(topic: String, discipline: String) {
        _topicInput.value = topic
        _selectedDiscipline.value = discipline
        generateResearchDraft()
    }

    fun generateResearchDraft() {
        val topic = _topicInput.value.trim()
        if (topic.isBlank()) {
            _uiState.value = ResearchUiState.Error("Please enter a research topic or question before generating.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ResearchUiState.Generating("Initializing academic draft synthesizer...", 0.1f)
            try {
                val result = repository.generatePaperDraft(
                    topic = topic,
                    academicLevel = _selectedAcademicLevel.value,
                    discipline = _selectedDiscipline.value
                ) { step, progress ->
                    _uiState.value = ResearchUiState.Generating(step, progress)
                }

                result.onSuccess { draft ->
                    _uiState.value = ResearchUiState.Success(draft)
                    historyRepository.saveDraft(draft)
                    _selectedTab.value = PaperSectionTab.FULL_PAPER
                }.onFailure { error ->
                    _uiState.value = ResearchUiState.Error(error.localizedMessage ?: "Failed to generate research paper draft.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = ResearchUiState.Error("Unexpected error: ${e.localizedMessage}")
            }
        }
    }

    fun loadDraftFromHistory(draft: ResearchPaperDraft) {
        _topicInput.value = draft.topic
        _selectedAcademicLevel.value = draft.academicLevel
        _selectedDiscipline.value = draft.discipline
        _uiState.value = ResearchUiState.Success(draft)
        _selectedTab.value = PaperSectionTab.FULL_PAPER
        _showHistorySheet.value = false
    }

    fun deleteDraft(draftId: String) {
        historyRepository.deleteDraft(draftId)
    }

    fun updateCurrentDraft(updated: ResearchPaperDraft) {
        _uiState.value = ResearchUiState.Success(updated)
        historyRepository.saveDraft(updated)
    }

    fun resetToNewTopic() {
        _uiState.value = ResearchUiState.Idle
        _topicInput.value = ""
        _selectedTab.value = PaperSectionTab.FULL_PAPER
    }
}
