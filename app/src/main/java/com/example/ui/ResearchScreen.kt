package com.example.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ResearchPaperDraft
import com.example.ui.components.AcademicHeader
import com.example.ui.components.EditDraftDialog
import com.example.ui.components.GeneratingProgressCard
import com.example.ui.components.NoticeBanner
import com.example.ui.components.PaperDraftView
import com.example.ui.components.SavedDraftsSheet
import com.example.ui.components.TopicInputSection
import com.example.util.ResearchPaperExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchScreen(
    viewModel: ResearchViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val topicInput by viewModel.topicInput.collectAsStateWithLifecycle()
    val academicLevel by viewModel.selectedAcademicLevel.collectAsStateWithLifecycle()
    val discipline by viewModel.selectedDiscipline.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedDrafts by viewModel.savedDrafts.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val showHistorySheet by viewModel.showHistorySheet.collectAsStateWithLifecycle()

    var draftToEdit by remember { mutableStateOf<ResearchPaperDraft?>(null) }
    var pendingSaveDraft by remember { mutableStateOf<ResearchPaperDraft?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // File Download / Save Launcher
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        if (uri != null && pendingSaveDraft != null) {
            ResearchPaperExporter.writeDraftToUri(context, uri, pendingSaveDraft!!)
        }
        pendingSaveDraft = null
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AcademicHeader(
                savedDraftsCount = savedDrafts.size,
                onOpenHistory = { viewModel.toggleHistorySheet(true) },
                onNewTopic = { viewModel.resetToNewTopic() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Notice Banner regarding academic template engine & Firebase
            NoticeBanner()

            // Input Section
            TopicInputSection(
                topic = topicInput,
                onTopicChange = viewModel::onTopicChange,
                selectedLevel = academicLevel,
                onLevelSelect = viewModel::onAcademicLevelSelect,
                selectedDiscipline = discipline,
                onDisciplineSelect = viewModel::onDisciplineSelect,
                onGenerate = viewModel::generateResearchDraft,
                onSelectExample = viewModel::selectExamplePrompt,
                isGenerating = uiState is ResearchUiState.Generating
            )

            // Dynamic Content based on State
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "stateContent"
            ) { state ->
                when (state) {
                    is ResearchUiState.Idle -> {
                        IdleWelcomeCard()
                    }
                    is ResearchUiState.Generating -> {
                        GeneratingProgressCard(
                            step = state.step,
                            progress = state.progress
                        )
                    }
                    is ResearchUiState.Success -> {
                        PaperDraftView(
                            draft = state.draft,
                            selectedTab = selectedTab,
                            onTabSelect = viewModel::onTabSelect,
                            onDownloadTxt = {
                                pendingSaveDraft = state.draft
                                val safeFilename = state.draft.topic
                                    .lowercase()
                                    .replace("[^a-z0-9]+".toRegex(), "_")
                                    .take(30)
                                createDocumentLauncher.launch("research_draft_${safeFilename}.txt")
                            },
                            onEditDraft = {
                                draftToEdit = state.draft
                            }
                        )
                    }
                    is ResearchUiState.Error -> {
                        ErrorCard(
                            message = state.message,
                            onRetry = viewModel::generateResearchDraft
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // History Bottom Sheet
        if (showHistorySheet) {
            SavedDraftsSheet(
                drafts = savedDrafts,
                onSelectDraft = viewModel::loadDraftFromHistory,
                onDeleteDraft = viewModel::deleteDraft,
                onDismiss = { viewModel.toggleHistorySheet(false) }
            )
        }

        // Edit Draft Dialog
        draftToEdit?.let { draft ->
            EditDraftDialog(
                draft = draft,
                onSave = { updated ->
                    viewModel.updateCurrentDraft(updated)
                    draftToEdit = null
                    Toast.makeText(context, "Draft updated!", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { draftToEdit = null }
            )
        }
    }
}

@Composable
fun IdleWelcomeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ready to Structure Your Research",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Enter any topic above or select one of the few-shot examples (AI in Education, Renewable Energy in Smart Cities) to generate an academic title, abstract, literature review guidance, objectives, methodology suggestions, and conclusion.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
