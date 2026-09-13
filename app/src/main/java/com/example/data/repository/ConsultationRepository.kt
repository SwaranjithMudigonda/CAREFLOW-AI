package com.example.data.repository

import com.example.data.ai.DefaultCareFlowAiEngine
import com.example.data.model.AIWarning
import com.example.data.model.ActionStatus
import com.example.data.model.ApprovalStatus
import com.example.data.model.CapturedDocument
import com.example.data.model.ClinicalEntity
import com.example.data.model.ClinicalNote
import com.example.data.model.Consultation
import com.example.data.model.DocumentType
import com.example.data.model.EntityCategory
import com.example.data.model.FollowUpAction
import com.example.data.model.FollowUpCategory
import com.example.data.model.Patient
import com.example.data.model.PatientSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ConsultationRepository(
    private val aiEngine: DefaultCareFlowAiEngine = DefaultCareFlowAiEngine()
) {
    private val _currentConsultation = MutableStateFlow<Consultation?>(null)
    val currentConsultation: StateFlow<Consultation?> = _currentConsultation.asStateFlow()

    private val _consultations = MutableStateFlow<List<Consultation>>(emptyList())
    val consultations: StateFlow<List<Consultation>> = _consultations.asStateFlow()

    private val _capturedDocuments = MutableStateFlow<List<CapturedDocument>>(emptyList())
    val capturedDocuments: StateFlow<List<CapturedDocument>> = _capturedDocuments.asStateFlow()

    init {
        seedInitialConsultations()
        seedInitialDocuments()
    }

    fun startNewConsultation(customPatient: Patient? = null): Consultation {
        val patient = customPatient ?: Patient(
            name = "Eleanor Vance",
            age = 42,
            gender = "Female",
            mrn = "MRN-77492"
        )
        val consultation = Consultation(
            id = UUID.randomUUID().toString(),
            patient = patient,
            approvalStatus = ApprovalStatus.DRAFT,
            isDemo = false
        )
        _currentConsultation.value = consultation
        return consultation
    }

    fun startDemoConsultation(): Consultation {
        val patient = Patient(
            name = "Sarah Jenkins",
            age = 38,
            gender = "Female",
            mrn = "MRN-88410"
        )
        val consultation = Consultation(
            id = UUID.randomUUID().toString(),
            patient = patient,
            transcript = DefaultCareFlowAiEngine.CANONICAL_DEMO_TRANSCRIPT,
            recordingDurationSec = 45,
            approvalStatus = ApprovalStatus.DRAFT,
            isDemo = true
        )
        _currentConsultation.value = consultation
        return consultation
    }

    fun setRecordingResult(transcript: String, durationSec: Int) {
        val current = _currentConsultation.value ?: startNewConsultation()
        _currentConsultation.value = current.copy(
            transcript = transcript,
            recordingDurationSec = durationSec
        )
    }

    suspend fun executeAiProcessing(onProgressUpdate: (String) -> Unit) {
        val current = _currentConsultation.value ?: return

        onProgressUpdate("Transcribing consultation...")
        val transcript = if (current.transcript.isNotBlank()) {
            current.transcript
        } else {
            aiEngine.transcribe(current.recordingDurationSec)
        }

        onProgressUpdate("Understanding clinical entities...")
        val (clinicalNote, entities) = aiEngine.extractClinicalNote(transcript)

        onProgressUpdate("Structuring clinical information...")
        val (followUps, warnings) = aiEngine.analyzeFollowUpActions(transcript, clinicalNote)

        onProgressUpdate("Detecting follow-up actions...")
        val patientSummary = aiEngine.generatePatientSummary(clinicalNote, followUps)

        _currentConsultation.value = current.copy(
            transcript = transcript,
            clinicalNote = clinicalNote,
            clinicalEntities = entities,
            followUpActions = followUps,
            warnings = warnings,
            patientSummary = patientSummary,
            approvalStatus = ApprovalStatus.AWAITING_REVIEW
        )
    }

    fun updateClinicalNote(updatedNote: ClinicalNote) {
        val current = _currentConsultation.value ?: return
        _currentConsultation.value = current.copy(clinicalNote = updatedNote)
    }

    fun updateActionStatus(actionId: String, status: ActionStatus, updatedTiming: String? = null) {
        val current = _currentConsultation.value ?: return
        val updatedList = current.followUpActions.map { action ->
            if (action.id == actionId) {
                action.copy(
                    status = status,
                    timing = updatedTiming ?: action.timing
                )
            } else action
        }
        _currentConsultation.value = current.copy(followUpActions = updatedList)
    }

    fun updateActionTiming(actionId: String, timing: String) {
        val current = _currentConsultation.value ?: return
        val updatedList = current.followUpActions.map { action ->
            if (action.id == actionId) {
                action.copy(
                    timing = timing,
                    status = ActionStatus.MODIFIED
                )
            } else action
        }
        _currentConsultation.value = current.copy(followUpActions = updatedList)
    }

    fun resolveAiWarning(warningId: String, resolution: String, timingForFollowup: String? = null) {
        val current = _currentConsultation.value ?: return
        val updatedWarnings = current.warnings.map { warning ->
            if (warning.id == warningId) {
                warning.copy(
                    isResolved = true,
                    resolutionChoice = resolution
                )
            } else warning
        }

        // If a timing was selected for the consultation action, apply it
        val updatedActions = if (timingForFollowup != null) {
            current.followUpActions.map { action ->
                if (action.category == FollowUpCategory.CONSULTATION) {
                    action.copy(
                        timing = timingForFollowup,
                        status = ActionStatus.MODIFIED
                    )
                } else action
            }
        } else {
            current.followUpActions
        }

        _currentConsultation.value = current.copy(
            warnings = updatedWarnings,
            followUpActions = updatedActions
        )
    }

    fun approveCarePlan(): Consultation? {
        val current = _currentConsultation.value ?: return null
        val finalizedSummary = current.patientSummary?.copy(
            isApproved = true,
            approvedByDoctorName = "Dr. S. Miller, MD",
            approvedTimestamp = System.currentTimeMillis()
        )
        val approved = current.copy(
            approvalStatus = ApprovalStatus.APPROVED,
            patientSummary = finalizedSummary
        )
        _currentConsultation.value = approved

        // Add or update in today's consultation list
        val existingIndex = _consultations.value.indexOfFirst { it.id == approved.id }
        val updatedList = if (existingIndex >= 0) {
            _consultations.value.toMutableList().apply { set(existingIndex, approved) }
        } else {
            listOf(approved) + _consultations.value
        }
        _consultations.value = updatedList
        return approved
    }

    fun saveDraft(): Consultation? {
        val current = _currentConsultation.value ?: return null
        val draft = current.copy(approvalStatus = ApprovalStatus.DRAFT)
        _currentConsultation.value = draft
        val existingIndex = _consultations.value.indexOfFirst { it.id == draft.id }
        val updatedList = if (existingIndex >= 0) {
            _consultations.value.toMutableList().apply { set(existingIndex, draft) }
        } else {
            listOf(draft) + _consultations.value
        }
        _consultations.value = updatedList
        return draft
    }

    suspend fun analyzeAndAddDocument(type: DocumentType, customLabel: String? = null): CapturedDocument {
        val doc = aiEngine.analyzeDocument(type, customLabel)
        _capturedDocuments.value = listOf(doc) + _capturedDocuments.value
        return doc
    }

    fun verifyDocument(docId: String) {
        _capturedDocuments.value = _capturedDocuments.value.map { doc ->
            if (doc.id == docId) doc.copy(clinicianVerified = true) else doc
        }
    }

    fun setCurrentConsultation(consultation: Consultation) {
        _currentConsultation.value = consultation
    }

    private fun seedInitialConsultations() {
        // Seed 8 realistic consultations for today:
        // Showing:
        // Today's consultations: 08
        // Pending follow-ups: 03
        // AI items awaiting review: 02
        val items = mutableListOf<Consultation>()

        // 1. Pending follow-up & awaiting review
        items.add(
            Consultation(
                id = "cons-01",
                patient = Patient(name = "Marcus Chen", age = 54, gender = "Male", mrn = "MRN-64219"),
                transcript = "Hypertension routine checkup. Medication adjusted to Lisinopril 20mg daily. Need blood pressure diary in 3 weeks.",
                approvalStatus = ApprovalStatus.AWAITING_REVIEW,
                clinicalNote = ClinicalNote(
                    chiefComplaint = "Hypertension follow-up",
                    duration = "Chronic",
                    symptoms = "Occasional morning headaches, no chest pain or edema.",
                    medications = "Lisinopril increased from 10mg to 20mg daily.",
                    investigations = "Renal function panel (BUN/Creatinine) ordered.",
                    assessmentPlan = "Essential hypertension, suboptimally controlled. Uptitrate Lisinopril. Check renal panel.",
                    followUp = "Return in 3 weeks with 14-day blood pressure log."
                ),
                followUpActions = listOf(
                    FollowUpAction(
                        category = FollowUpCategory.MEDICATION,
                        title = "Lisinopril 20mg",
                        description = "Take 1 tablet daily every morning.",
                        timing = "Daily morning",
                        whyReason = "Dosage uptitrated during visit.",
                        status = ActionStatus.PENDING_REVIEW
                    ),
                    FollowUpAction(
                        category = FollowUpCategory.CONSULTATION,
                        title = "BP Review Visit",
                        description = "Evaluate response to higher Lisinopril dose.",
                        timing = "In 3 weeks",
                        whyReason = "Clinician requested 3-week BP check.",
                        status = ActionStatus.PENDING_REVIEW
                    )
                ),
                warnings = listOf(
                    AIWarning(
                        title = "Renal panel timing pending",
                        warningText = "Renal function test ordered without collection deadline.",
                        whyReason = "Lab order extracted but target draw date was not articulated.",
                        recommendation = "Specify draw window (e.g. 10 days post dose increase).",
                        isResolved = false
                    )
                )
            )
        )

        // 2. Another pending AI item
        items.add(
            Consultation(
                id = "cons-02",
                patient = Patient(name = "Maria Rodriguez", age = 29, gender = "Female", mrn = "MRN-91244"),
                transcript = "Acute bronchitis symptoms for 5 days. Productive cough, mild wheeze. Prescribed inhaler.",
                approvalStatus = ApprovalStatus.AWAITING_REVIEW,
                clinicalNote = ClinicalNote(
                    chiefComplaint = "Acute cough and chest tightness",
                    duration = "5 days",
                    symptoms = "Productive cough with clear sputum, mild exertion wheeze.",
                    medications = "Albuterol HFA 90mcg 2 puffs q4-6h prn cough.",
                    investigations = "Chest X-ray completed: Clear lungs.",
                    assessmentPlan = "Acute acute bronchitis, viral etiology. Supportive therapy.",
                    followUp = "Return if fever develops or symptoms exceed 10 days."
                ),
                followUpActions = listOf(
                    FollowUpAction(
                        category = FollowUpCategory.MEDICATION,
                        title = "Albuterol Inhaler",
                        description = "1-2 puffs every 4-6 hours as needed.",
                        timing = "PRN for 7 days",
                        whyReason = "Prescribed for bronchospasm relief.",
                        status = ActionStatus.PENDING_REVIEW
                    )
                ),
                warnings = emptyList()
            )
        )

        // 3. Pending follow-up (Approved)
        items.add(
            Consultation(
                id = "cons-03",
                patient = Patient(name = "David Park", age = 67, gender = "Male", mrn = "MRN-33108"),
                approvalStatus = ApprovalStatus.APPROVED,
                followUpActions = listOf(
                    FollowUpAction(
                        category = FollowUpCategory.INVESTIGATION,
                        title = "HbA1c & Lipid Panel",
                        description = "Quarterly diabetic surveillance panel.",
                        timing = "In 4 weeks",
                        whyReason = "Scheduled 3-month type 2 diabetes monitoring.",
                        status = ActionStatus.ACCEPTED
                    )
                )
            )
        )

        // 4-8. Approved standard consultations
        val otherPatients = listOf(
            Pair("Emma Watson", "MRN-55219"),
            Pair("Robert Taylor", "MRN-88401"),
            Pair("Anita Patel", "MRN-44120"),
            Pair("James O'Connor", "MRN-19455"),
            Pair("Grace Kim", "MRN-72314")
        )

        otherPatients.forEachIndexed { index, (name, mrn) ->
            items.add(
                Consultation(
                    id = "cons-0${index + 4}",
                    patient = Patient(name = name, age = 30 + index * 5, gender = if (index % 2 == 0) "Female" else "Male", mrn = mrn),
                    transcript = "Standard outpatient consultation completed. Documentation signed.",
                    approvalStatus = ApprovalStatus.APPROVED
                )
            )
        }

        _consultations.value = items
    }

    private fun seedInitialDocuments() {
        _capturedDocuments.value = listOf(
            CapturedDocument(
                id = "doc-01",
                type = DocumentType.LAB_REPORT,
                title = "Metabolic Panel - Eleanor Vance",
                detectedEntities = listOf(
                    "HbA1c: 5.6%",
                    "eGFR: >90 mL/min",
                    "Sodium: 140 mEq/L",
                    "Potassium: 4.2 mEq/L"
                ),
                summaryText = "Standard comprehensive chemistry panel within normal limits.",
                dateString = "Today, 09:15 AM",
                clinicianVerified = true
            )
        )
    }
}
