package com.example.data.model

import java.util.UUID

data class Patient(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val age: Int,
    val gender: String,
    val mrn: String // Medical Record Number
)

enum class EntityCategory {
    SYMPTOM,
    DURATION,
    MEDICATION,
    INVESTIGATION,
    FOLLOW_UP,
    DIAGNOSIS
}

data class ClinicalEntity(
    val id: String = UUID.randomUUID().toString(),
    val category: EntityCategory,
    val text: String,
    val details: String? = null,
    val isConfirmed: Boolean = true
)

data class ClinicalNote(
    val chiefComplaint: String = "",
    val duration: String = "",
    val symptoms: String = "",
    val relevantHistory: String = "",
    val medications: String = "",
    val investigations: String = "",
    val assessmentPlan: String = "",
    val followUp: String = ""
)

enum class FollowUpCategory {
    MEDICATION,
    INVESTIGATION,
    CONSULTATION,
    LIFESTYLE
}

enum class ActionStatus {
    PENDING_REVIEW,
    ACCEPTED,
    REJECTED,
    MODIFIED
}

data class FollowUpAction(
    val id: String = UUID.randomUUID().toString(),
    val category: FollowUpCategory,
    val title: String,
    val description: String,
    val timing: String? = null,
    val whyReason: String, // Explainable AI
    val status: ActionStatus = ActionStatus.PENDING_REVIEW
)

data class AIWarning(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val warningText: String,
    val whyReason: String, // "Why was this flagged?"
    val recommendation: String, // "Consider specifying..."
    val isResolved: Boolean = false,
    val resolutionChoice: String? = null // e.g. "2 weeks", "Keep as is", "Dismissed"
)

data class PatientSummary(
    val id: String = UUID.randomUUID().toString(),
    val plainLanguageText: String,
    val keyActions: List<String>,
    val isApproved: Boolean = false,
    val approvedByDoctorName: String? = null,
    val approvedTimestamp: Long? = null
)

enum class DocumentType {
    LAB_REPORT,
    PRESCRIPTION,
    CLINICAL_NOTE
}

data class CapturedDocument(
    val id: String = UUID.randomUUID().toString(),
    val type: DocumentType,
    val title: String,
    val detectedEntities: List<String>,
    val summaryText: String,
    val dateString: String,
    val clinicianVerified: Boolean = false
)

data class ClinicalDocument(
    val id: String = UUID.randomUUID().toString(),
    val documentType: String,
    val title: String,
    val keyFindings: List<String>,
    val detectedActions: List<FollowUpAction>,
    val missingInformationWarning: String? = null,
    val clinicianVerified: Boolean = false
)


enum class ApprovalStatus {
    DRAFT,
    PROCESSING,
    AWAITING_REVIEW,
    APPROVED
}

data class Consultation(
    val id: String = UUID.randomUUID().toString(),
    val patient: Patient,
    val timestamp: Long = System.currentTimeMillis(),
    val transcript: String = "",
    val recordingDurationSec: Int = 0,
    val clinicalNote: ClinicalNote = ClinicalNote(),
    val clinicalEntities: List<ClinicalEntity> = emptyList(),
    val followUpActions: List<FollowUpAction> = emptyList(),
    val warnings: List<AIWarning> = emptyList(),
    val patientSummary: PatientSummary? = null,
    val approvalStatus: ApprovalStatus = ApprovalStatus.DRAFT,
    val isDemo: Boolean = false
)
