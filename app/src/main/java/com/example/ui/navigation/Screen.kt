package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object NewConsultation : Screen("new_consultation")
    object RecordConsultation : Screen("record_consultation")
    object Processing : Screen("processing")
    object ClinicalNote : Screen("clinical_note")
    object FollowUpIntelligence : Screen("follow_up_intelligence")
    object ClinicianReview : Screen("clinician_review")
    object PatientSummary : Screen("patient_summary")
    object CaptureDocument : Screen("capture_document")
    object Settings : Screen("settings")
}
