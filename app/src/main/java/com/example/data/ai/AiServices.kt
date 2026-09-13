package com.example.data.ai

import com.example.data.model.AIWarning
import com.example.data.model.CapturedDocument
import com.example.data.model.ClinicalEntity
import com.example.data.model.ClinicalNote
import com.example.data.model.DocumentType
import com.example.data.model.FollowUpAction
import com.example.data.model.PatientSummary

interface TranscriptionService {
    suspend fun transcribe(durationSec: Int, customInput: String? = null): String
}

interface ClinicalExtractionService {
    suspend fun extractClinicalNote(transcript: String): Pair<ClinicalNote, List<ClinicalEntity>>
}

interface FollowUpAnalysisService {
    suspend fun analyzeFollowUpActions(
        transcript: String,
        clinicalNote: ClinicalNote
    ): Pair<List<FollowUpAction>, List<AIWarning>>
}

interface DocumentAnalysisService {
    suspend fun analyzeDocument(
        type: DocumentType,
        customLabel: String? = null
    ): CapturedDocument
}

interface PatientSummaryService {
    suspend fun generatePatientSummary(
        clinicalNote: ClinicalNote,
        followUpActions: List<FollowUpAction>
    ): PatientSummary
}
