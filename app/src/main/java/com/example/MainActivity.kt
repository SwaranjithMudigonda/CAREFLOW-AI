package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.ApprovalStatus
import com.example.ui.navigation.Screen
import com.example.ui.screens.CaptureDocumentScreen
import com.example.ui.screens.ClinicalNoteScreen
import com.example.ui.screens.ClinicianReviewScreen
import com.example.ui.screens.FollowUpIntelligenceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NewConsultationScreen
import com.example.ui.screens.PatientSummaryScreen
import com.example.ui.screens.ProcessingScreen
import com.example.ui.screens.RecordConsultationScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CareFlowTheme
import com.example.ui.viewmodel.CareFlowViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CareFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CareFlowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CareFlowNavGraph(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CareFlowNavGraph(
    viewModel: CareFlowViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // 1. Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToNewConsultation = {
                    navController.navigate(Screen.NewConsultation.route)
                },
                onNavigateToRecord = {
                    navController.navigate(Screen.RecordConsultation.route)
                },
                onStartDemoFlow = {
                    viewModel.startDemoConsultation()
                    navController.navigate(Screen.Processing.route)
                },
                onNavigateToCaptureDoc = {
                    navController.navigate(Screen.CaptureDocument.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onSelectConsultation = { consultation ->
                    viewModel.selectConsultation(consultation)
                    when (consultation.approvalStatus) {
                        ApprovalStatus.APPROVED -> navController.navigate(Screen.PatientSummary.route)
                        ApprovalStatus.AWAITING_REVIEW -> navController.navigate(Screen.ClinicianReview.route)
                        ApprovalStatus.DRAFT, ApprovalStatus.PROCESSING -> navController.navigate(Screen.ClinicalNote.route)
                    }
                }
            )
        }

        // 2. New Consultation Screen
        composable(Screen.NewConsultation.route) {
            NewConsultationScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToRecord = {
                    navController.navigate(Screen.RecordConsultation.route)
                },
                onNavigateToCaptureDoc = {
                    navController.navigate(Screen.CaptureDocument.route)
                },
                onProceedWithDemo = {
                    navController.navigate(Screen.Processing.route)
                }
            )
        }

        // 3. Record Consultation Screen
        composable(Screen.RecordConsultation.route) {
            RecordConsultationScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToProcessing = {
                    navController.navigate(Screen.Processing.route)
                }
            )
        }

        // 4. Processing Screen
        composable(Screen.Processing.route) {
            ProcessingScreen(
                viewModel = viewModel,
                onProcessingFinished = {
                    navController.navigate(Screen.ClinicalNote.route) {
                        popUpTo(Screen.RecordConsultation.route) { inclusive = true }
                        popUpTo(Screen.NewConsultation.route) { inclusive = true }
                    }
                }
            )
        }

        // 5. Clinical Note Screen
        composable(Screen.ClinicalNote.route) {
            ClinicalNoteScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onContinueToFollowUp = {
                    navController.navigate(Screen.FollowUpIntelligence.route)
                }
            )
        }

        // 6. Follow-Up Intelligence Screen (CORE DIFFERENTIATOR)
        composable(Screen.FollowUpIntelligence.route) {
            FollowUpIntelligenceScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToReview = {
                    navController.navigate(Screen.ClinicianReview.route)
                }
            )
        }

        // 7. Clinician Review Screen
        composable(Screen.ClinicianReview.route) {
            ClinicianReviewScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onApproved = {
                    navController.navigate(Screen.PatientSummary.route)
                },
                onSavedDraft = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // 8. Patient Summary Screen
        composable(Screen.PatientSummary.route) {
            PatientSummaryScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // 9. Capture Clinical Document Screen
        composable(Screen.CaptureDocument.route) {
            CaptureDocumentScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onDocumentMerged = {
                    navController.navigate(Screen.ClinicalNote.route)
                }
            )
        }

        // 10. Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
