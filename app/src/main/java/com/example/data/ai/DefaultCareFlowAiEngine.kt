package com.example.data.ai

import com.example.data.model.AIWarning
import com.example.data.model.CapturedDocument
import com.example.data.model.ClinicalEntity
import com.example.data.model.ClinicalNote
import com.example.data.model.DocumentType
import com.example.data.model.EntityCategory
import com.example.data.model.FollowUpAction
import com.example.data.model.FollowUpCategory
import com.example.data.model.PatientSummary
import kotlinx.coroutines.delay

class DefaultCareFlowAiEngine :
    TranscriptionService,
    ClinicalExtractionService,
    FollowUpAnalysisService,
    DocumentAnalysisService,
    PatientSummaryService {

    companion object {
        const val CANONICAL_DEMO_TRANSCRIPT =
            "Patient reports fatigue for two weeks. No major additional symptoms reported. Doctor recommends a blood test and asks the patient to return after the results. Medication X should be taken twice daily."
    }

    override suspend fun transcribe(durationSec: Int, customInput: String?): String {
        delay(600) // Realistic asynchronous transcription delay
        return customInput?.takeIf { it.isNotBlank() } ?: CANONICAL_DEMO_TRANSCRIPT
    }

    override suspend fun extractClinicalNote(transcript: String): Pair<ClinicalNote, List<ClinicalEntity>> {
        delay(700) // Realistic clinical extraction delay

        val isCanonical = transcript.contains("fatigue", ignoreCase = true)

        val note = if (isCanonical) {
            ClinicalNote(
                chiefComplaint = "Fatigue",
                duration = "2 weeks",
                symptoms = "Persistent generalized fatigue for two weeks. Patient reports no major additional symptoms (no fever, night sweats, dyspnea, or syncope).",
                relevantHistory = "Nil reported / Non-contributory past medical history.",
                medications = "Medication X - Twice daily (oral)",
                investigations = "Diagnostic blood test (Complete Blood Count and Metabolic Profile requested)",
                assessmentPlan = "1. Fatigue of two weeks duration, etiology under evaluation.\n2. Prescribed Medication X to be taken twice daily.\n3. Ordered diagnostic blood testing.\n4. Plan for clinical re-evaluation once laboratory findings are available.",
                followUp = "Return after blood test results are ready."
            )
        } else {
            ClinicalNote(
                chiefComplaint = "Clinical Consultation Evaluation",
                duration = "As recorded",
                symptoms = transcript.take(120),
                relevantHistory = "Reviewed during clinical encounter.",
                medications = "Per clinical discussion.",
                investigations = "Laboratory investigations ordered.",
                assessmentPlan = "Clinical plan formulated with patient. Actions pending review.",
                followUp = "Follow-up consultation recommended."
            )
        }

        val entities = if (isCanonical) {
            listOf(
                ClinicalEntity(
                    category = EntityCategory.SYMPTOM,
                    text = "Fatigue",
                    details = "Primary symptom"
                ),
                ClinicalEntity(
                    category = EntityCategory.DURATION,
                    text = "2 weeks",
                    details = "Onset & persistence"
                ),
                ClinicalEntity(
                    category = EntityCategory.MEDICATION,
                    text = "Medication X",
                    details = "Dosage: Twice daily"
                ),
                ClinicalEntity(
                    category = EntityCategory.INVESTIGATION,
                    text = "Blood test",
                    details = "Diagnostic laboratory workup"
                ),
                ClinicalEntity(
                    category = EntityCategory.FOLLOW_UP,
                    text = "Required",
                    details = "Post-investigation review"
                )
            )
        } else {
            listOf(
                ClinicalEntity(
                    category = EntityCategory.SYMPTOM,
                    text = "Reported Symptoms",
                    details = "From transcription"
                ),
                ClinicalEntity(
                    category = EntityCategory.FOLLOW_UP,
                    text = "Review required",
                    details = "Scheduled"
                )
            )
        }

        return Pair(note, entities)
    }

    override suspend fun analyzeFollowUpActions(
        transcript: String,
        clinicalNote: ClinicalNote
    ): Pair<List<FollowUpAction>, List<AIWarning>> {
        delay(600) // Analysis delay

        val actions = listOf(
            FollowUpAction(
                category = FollowUpCategory.MEDICATION,
                title = "Medication Instruction",
                description = "Medication X - Take 1 dose twice daily as directed.",
                timing = "Twice daily",
                whyReason = "The consultation contains an explicit medication instruction."
            ),
            FollowUpAction(
                category = FollowUpCategory.INVESTIGATION,
                title = "Blood Test",
                description = "Complete outpatient venous blood draw (CBC & metabolic evaluation).",
                timing = "Before return appointment",
                whyReason = "Doctor recommended a blood test to evaluate reported symptoms."
            ),
            FollowUpAction(
                category = FollowUpCategory.CONSULTATION,
                title = "Follow-up Consultation",
                description = "In-clinic appointment to review lab results and monitor response to Medication X.",
                timing = "Pending timing confirmation",
                whyReason = "Doctor asked the patient to return after blood test results are received."
            )
        )

        val warnings = listOf(
            AIWarning(
                title = "Follow-up timing is not explicitly specified",
                warningText = "Doctor said: \"Return after the blood test.\"",
                whyReason = "The consultation specifies that the patient should return after the blood test, but no follow-up timeframe was stated.",
                recommendation = "Consider specifying when the follow-up should occur."
            )
        )

        return Pair(actions, warnings)
    }

    override suspend fun analyzeDocument(
        type: DocumentType,
        customLabel: String?
    ): CapturedDocument {
        delay(700)
        return when (type) {
            DocumentType.LAB_REPORT -> CapturedDocument(
                type = DocumentType.LAB_REPORT,
                title = customLabel ?: "Diagnostic Lab Report",
                detectedEntities = listOf(
                    "HbA1c: 5.6% (Normal < 5.7%)",
                    "Hemoglobin: 13.8 g/dL (Normal 12.0 - 15.5)",
                    "Platelet Count: 245 k/uL (Normal 150 - 450)",
                    "Fasting Glucose: 92 mg/dL (Normal 70 - 99)",
                    "Sample Date: September 2026"
                ),
                summaryText = "Automated optical extraction completed. Key hematology markers appear within standard physiological reference ranges.",
                dateString = "Recorded: Today",
                clinicianVerified = false
            )
            DocumentType.PRESCRIPTION -> CapturedDocument(
                type = DocumentType.PRESCRIPTION,
                title = customLabel ?: "Prescription Order Slip",
                detectedEntities = listOf(
                    "Rx: Medication X 50 mg",
                    "Sig: Take 1 tablet twice daily orally",
                    "Dispense: 30-day supply",
                    "Refills: 01",
                    "Date: Current encounter"
                ),
                summaryText = "Verified outpatient medication order matching active consultation notes.",
                dateString = "Recorded: Today",
                clinicianVerified = false
            )
            DocumentType.CLINICAL_NOTE -> CapturedDocument(
                type = DocumentType.CLINICAL_NOTE,
                title = customLabel ?: "External Clinical Note",
                detectedEntities = listOf(
                    "Encounter: Outpatient General Medicine",
                    "Subjective: Persistent fatigue x 14 days",
                    "Vitals: BP 120/80 mmHg, HR 72 bpm, SpO2 99%",
                    "Plan: Diagnostic laboratory workup"
                ),
                summaryText = "Standard clinical documentation draft structured for EHR transfer.",
                dateString = "Recorded: Today",
                clinicianVerified = false
            )
        }
    }

    override suspend fun generatePatientSummary(
        clinicalNote: ClinicalNote,
        followUpActions: List<FollowUpAction>
    ): PatientSummary {
        delay(500)
        return PatientSummary(
            plainLanguageText = "Your doctor has requested a blood test. Please complete the test and return for follow-up after your results are available.",
            keyActions = listOf(
                "Take Medication X twice daily with meals or water as instructed.",
                "Visit the outpatient lab for your scheduled blood test.",
                "Schedule a follow-up appointment once your blood test results are ready."
            ),
            isApproved = false,
            approvedByDoctorName = "Dr. S. Miller, MD",
            approvedTimestamp = null
        )
    }
}
