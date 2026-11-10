package com.example.data

import com.example.model.ResearchPaperDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DraftHistoryRepository {

    private val _drafts = MutableStateFlow<List<ResearchPaperDraft>>(emptyList())
    val drafts: StateFlow<List<ResearchPaperDraft>> = _drafts.asStateFlow()

    fun saveDraft(draft: ResearchPaperDraft) {
        val current = _drafts.value.toMutableList()
        // If a draft with the same ID or same topic already exists, replace or prepend
        val existingIndex = current.indexOfFirst { it.id == draft.id }
        if (existingIndex >= 0) {
            current[existingIndex] = draft
        } else {
            current.add(0, draft)
        }
        _drafts.value = current
    }

    fun deleteDraft(draftId: String) {
        _drafts.value = _drafts.value.filterNot { it.id == draftId }
    }

    fun clearAll() {
        _drafts.value = emptyList()
    }
}
