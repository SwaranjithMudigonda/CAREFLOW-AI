package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AIWarning
import com.example.data.model.ActionStatus
import com.example.data.model.ApprovalStatus
import com.example.data.model.CapturedDocument
import com.example.data.model.ClinicalNote
import com.example.data.model.Consultation
import com.example.data.model.DocumentType
import com.example.data.model.FollowUpAction
import com.example.data.model.Patient
import com.example.data.repository.ConsultationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardMetrics(
    val todayConsultationsCount: Int = 8,
    val pendingFollowUpsCount: Int = 3,
    val aiItemsAwaitingReviewCount: Int = 2
)

data class ExplainableInfo(
    val title: String,
    val headline: String,
    val whyExplanation: String,
    val sourceContext: String,
    val recommendation: String? = null
)

class CareFlowViewModel(
    private val repository: ConsultationRepository = ConsultationRepository()
) : ViewModel() {

    val currentConsultation: StateFlow<Consultation?> = repository.currentConsultation
    val consultations: StateFlow<List<Consultation>> = repository.consultations
    val capturedDocuments: StateFlow<List<CapturedDocument>> = repository.capturedDocuments

    // Dashboard metrics
    val metrics: StateFlow<DashboardMetrics> = combine(
        consultations,
        currentConsultation
    ) { list, _ ->
        val todayCount = list.size
        val pendingReviewCount = list.count { it.approvalStatus == ApprovalStatus.AWAITING_REVIEW }
        val pendingFollowups = list.flatMap { it.followUpActions }
            .count { it.status == ActionStatus.PENDING_REVIEW || it.status == ActionStatus.ACCEPTED }
            .coerceAtLeast(3)

        DashboardMetrics(
            todayConsultationsCount = todayCount.coerceAtLeast(8),
            pendingFollowUpsCount = pendingFollowups,
            aiItemsAwaitingReviewCount = pendingReviewCount.coerceAtLeast(2)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardMetrics()
    )

    // Audio recording state
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private val _recordingCaptured = MutableStateFlow(false)
    val recordingCaptured: StateFlow<Boolean> = _recordingCaptured.asStateFlow()

    private var timerJob: Job? = null

    // Processing progress state
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingStage = MutableStateFlow("Preparing clinical workflow...")
    val processingStage: StateFlow<String> = _processingStage.asStateFlow()

    // Document capture state
    private val _isAnalyzingDocument = MutableStateFlow(false)
    val isAnalyzingDocument: StateFlow<Boolean> = _isAnalyzingDocument.asStateFlow()

    private val _analyzedDocument = MutableStateFlow<com.example.data.model.ClinicalDocument?>(null)
    val analyzedDocument: StateFlow<com.example.data.model.ClinicalDocument?> = _analyzedDocument.asStateFlow()

    // Dialog state for Explainable AI
    private val _activeExplanation = MutableStateFlow<ExplainableInfo?>(null)
    val activeExplanation: StateFlow<ExplainableInfo?> = _activeExplanation.asStateFlow()

    // Dialog state for Timing Selection
    private val _activeTimingWarning = MutableStateFlow<AIWarning?>(null)
    val activeTimingWarning: StateFlow<AIWarning?> = _activeTimingWarning.asStateFlow()

    fun startNewConsultation(patient: Patient? = null) {
        resetRecordingState()
        repository.startNewConsultation(patient)
    }

    fun startDemoConsultation() {
        resetRecordingState()
        repository.startDemoConsultation()
    }

    fun startRecording() {
        _isRecording.value = true
        _isPaused.value = false
        _recordingCaptured.value = false
        _recordingSeconds.value = 0

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isRecording.value) {
                delay(1000)
                if (!_isPaused.value) {
                    _recordingSeconds.value += 1
                }
            }
        }
    }

    fun pauseRecording() {
        _isPaused.value = true
    }

    fun resumeRecording() {
        _isPaused.value = false
    }

    fun stopRecording(customTranscript: String? = null) {
        _isRecording.value = false
        _isPaused.value = false
        _recordingCaptured.value = true
        timerJob?.cancel()

        val transcript = customTranscript
            ?: "Patient reports fatigue for two weeks. No major additional symptoms reported. Doctor recommends a blood test and asks the patient to return after the results. Medication X should be taken twice daily."

        repository.setRecordingResult(
            transcript = transcript,
            durationSec = _recordingSeconds.value.coerceAtLeast(12)
        )
    }

    fun cancelRecording() {
        resetRecordingState()
    }

    private fun resetRecordingState() {
        _isRecording.value = false
        _isPaused.value = false
        _recordingCaptured.value = false
        _recordingSeconds.value = 0
        timerJob?.cancel()
    }

    fun runAiProcessing(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isProcessing.value = true
            repository.executeAiProcessing { stageText ->
                _processingStage.value = stageText
            }
            delay(300)
            _isProcessing.value = false
            onComplete()
        }
    }

    fun updateClinicalNote(note: ClinicalNote) {
        repository.updateClinicalNote(note)
    }

    fun updateActionStatus(actionId: String, status: ActionStatus) {
        repository.updateActionStatus(actionId, status)
    }

    fun updateActionTiming(actionId: String, timing: String) {
        repository.updateActionTiming(actionId, timing)
    }

    fun openExplanation(info: ExplainableInfo) {
        _activeExplanation.value = info
    }

    fun closeExplanation() {
        _activeExplanation.value = null
    }

    fun openTimingDialog(warning: AIWarning) {
        _activeTimingWarning.value = warning
    }

    fun closeTimingDialog() {
        _activeTimingWarning.value = null
    }

    fun resolveWarningWithTiming(warningId: String, timingChoice: String) {
        repository.resolveAiWarning(
            warningId = warningId,
            resolution = "Timing specified: $timingChoice",
            timingForFollowup = timingChoice
        )
        closeTimingDialog()
    }

    fun keepWarningAsIs(warningId: String) {
        repository.resolveAiWarning(
            warningId = warningId,
            resolution = "Clinician accepted as unspecified"
        )
        closeTimingDialog()
    }

    fun dismissWarning(warningId: String) {
        repository.resolveAiWarning(
            warningId = warningId,
            resolution = "Dismissed by clinician"
        )
        closeTimingDialog()
    }

    fun approveCarePlan(): Consultation? {
        return repository.approveCarePlan()
    }

    fun saveDraft(): Consultation? {
        return repository.saveDraft()
    }

    fun captureDocument(type: DocumentType, customLabel: String? = null, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isAnalyzingDocument.value = true
            repository.analyzeAndAddDocument(type, customLabel)
            _isAnalyzingDocument.value = false
            onComplete()
        }
    }

    fun analyzeDocument(sampleTitle: String) {
        viewModelScope.launch {
            _isAnalyzingDocument.value = true
            delay(1200)

            val doc = when {
                sampleTitle.contains("Lab", ignoreCase = true) || sampleTitle.contains("Blood", ignoreCase = true) -> {
                    com.example.data.model.ClinicalDocument(
                        documentType = "Laboratory Report",
                        title = sampleTitle,
                        keyFindings = listOf(
                            "Hemoglobin: 11.2 g/dL (Mild microcytic anemia)",
                            "Serum Ferritin: 14 ng/mL (Low iron stores)",
                            "TSH: 2.1 mIU/L (Within normal limits)"
                        ),
                        detectedActions = listOf(
                            FollowUpAction(
                                title = "Oral Iron Supplementation",
                                description = "Prescribe Ferrous Sulfate 325mg daily with vitamin C.",
                                category = com.example.data.model.FollowUpCategory.MEDICATION,
                                timing = "Daily for 3 months",
                                status = ActionStatus.PENDING_REVIEW,
                                whyReason = "Low serum ferritin of 14 ng/mL indicates iron deficiency."
                            ),
                            FollowUpAction(
                                title = "Repeat CBC & Ferritin Panel",
                                description = "Evaluate response to oral iron therapy and normalization of hemoglobin.",
                                category = com.example.data.model.FollowUpCategory.INVESTIGATION,
                                timing = "In 8 weeks",
                                status = ActionStatus.PENDING_REVIEW,
                                whyReason = "Required clinical monitoring interval for microcytic anemia."
                            )
                        ),
                        missingInformationWarning = "Report lacks patient fasting confirmation at time of draw."
                    )
                }
                sampleTitle.contains("Referral", ignoreCase = true) -> {
                    com.example.data.model.ClinicalDocument(
                        documentType = "Cardiology Referral",
                        title = sampleTitle,
                        keyFindings = listOf(
                            "History of episodic palpitations during moderate exertion",
                            "Resting 12-lead ECG: Normal sinus rhythm, rate 74 bpm",
                            "Echocardiogram: Normal LV systolic function, EF 60%"
                        ),
                        detectedActions = listOf(
                            FollowUpAction(
                                title = "Holter Monitoring 48-Hour",
                                description = "Correlate ambulatory heart rhythm with symptom diary.",
                                category = com.example.data.model.FollowUpCategory.INVESTIGATION,
                                timing = "Within 14 days",
                                status = ActionStatus.PENDING_REVIEW,
                                whyReason = "Transient palpitations uncaptured on resting 12-lead ECG."
                            )
                        )
                    )
                }
                else -> {
                    com.example.data.model.ClinicalDocument(
                        documentType = "Discharge Summary",
                        title = sampleTitle,
                        keyFindings = listOf(
                            "Discharge Diagnosis: Resolving viral gastroenteritis with mild dehydration",
                            "Oral rehydration therapy tolerated well prior to discharge",
                            "Electrolytes stabilized"
                        ),
                        detectedActions = listOf(
                            FollowUpAction(
                                title = "Outpatient Primary Care Follow-up",
                                description = "Assess hydration status, weight recovery, and dietary progression.",
                                category = com.example.data.model.FollowUpCategory.CONSULTATION,
                                timing = "In 7 days",
                                status = ActionStatus.PENDING_REVIEW,
                                whyReason = "Post-acute discharge safety check."
                            )
                        )
                    )
                }
            }

            _analyzedDocument.value = doc
            _isAnalyzingDocument.value = false
        }
    }

    fun mergeDocumentIntoConsultation() {
        val doc = _analyzedDocument.value ?: return
        val current = currentConsultation.value ?: return

        val updatedActions = current.followUpActions.toMutableList()
        doc.detectedActions.forEach { action ->
            if (updatedActions.none { it.title == action.title }) {
                updatedActions.add(action)
            }
        }

        val updatedNote = current.clinicalNote.copy(
            investigations = if (current.clinicalNote.investigations.isNotBlank()) {
                "${current.clinicalNote.investigations}\nAttached Document: ${doc.title} (${doc.keyFindings.joinToString("; ")})"
            } else {
                "Attached Document: ${doc.title} (${doc.keyFindings.joinToString("; ")})"
            }
        )

        repository.updateClinicalNote(updatedNote)
        updatedActions.forEach { repository.updateActionStatus(it.id, it.status) }
        _analyzedDocument.value = null
    }

    fun resetToInitialDemo() {
        startDemoConsultation()
    }

    fun verifyDocument(docId: String) {
        repository.verifyDocument(docId)
    }

    fun selectConsultation(consultation: Consultation) {
        repository.setCurrentConsultation(consultation)
    }
}

