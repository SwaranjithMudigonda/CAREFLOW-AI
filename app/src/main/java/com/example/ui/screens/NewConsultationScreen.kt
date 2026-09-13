package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Patient
import com.example.ui.components.CareFlowTopAppBar
import com.example.ui.viewmodel.CareFlowViewModel

enum class ConsultationOption {
    RECORD_CONSULTATION,
    USE_DEMO_CONSULTATION,
    CAPTURE_CLINICAL_DOCUMENT
}

@Composable
fun NewConsultationScreen(
    viewModel: CareFlowViewModel,
    onBackClick: () -> Unit,
    onNavigateToRecord: () -> Unit,
    onNavigateToCaptureDoc: () -> Unit,
    onProceedWithDemo: () -> Unit
) {
    var selectedOption by remember { mutableStateOf(ConsultationOption.RECORD_CONSULTATION) }
    var patientName by remember { mutableStateOf("Sarah Jenkins") }
    var patientAge by remember { mutableStateOf("38") }
    var patientMrn by remember { mutableStateOf("MRN-88410") }

    Scaffold(
        topBar = {
            CareFlowTopAppBar(
                title = "New Consultation",
                subtitle = "Initiate clinical audio or document intake",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val patient = Patient(
                            name = patientName.ifBlank { "Patient Demo" },
                            age = patientAge.toIntOrNull() ?: 38,
                            gender = "Female",
                            mrn = patientMrn.ifBlank { "MRN-88410" }
                        )

                        when (selectedOption) {
                            ConsultationOption.RECORD_CONSULTATION -> {
                                viewModel.startNewConsultation(patient)
                                onNavigateToRecord()
                            }
                            ConsultationOption.USE_DEMO_CONSULTATION -> {
                                viewModel.startDemoConsultation()
                                onProceedWithDemo()
                            }
                            ConsultationOption.CAPTURE_CLINICAL_DOCUMENT -> {
                                viewModel.startNewConsultation(patient)
                                onNavigateToCaptureDoc()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_consultation_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Start Consultation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Patient Identity Card (Header)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Patient Demographics",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = patientName,
                            onValueChange = { patientName = it },
                            label = { Text("Patient Name") },
                            modifier = Modifier.weight(2f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = patientAge,
                            onValueChange = { patientAge = it },
                            label = { Text("Age") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Select Consultation Modality",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Option A: Record Consultation
            ModalityOptionCard(
                title = "Record Consultation",
                description = "Capture live doctor-patient conversation via phone microphone with real-time waveform visualization.",
                icon = Icons.Default.Mic,
                isSelected = selectedOption == ConsultationOption.RECORD_CONSULTATION,
                onClick = { selectedOption = ConsultationOption.RECORD_CONSULTATION },
                testTag = "option_record_consultation"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option B: Use Demo Consultation
            ModalityOptionCard(
                title = "Use Demo Consultation",
                badge = "Hackathon Ready",
                description = "Instantly load the canonical fatigue & blood test case. Works 100% reliably without requiring microphone access.",
                icon = Icons.Default.AutoAwesome,
                isSelected = selectedOption == ConsultationOption.USE_DEMO_CONSULTATION,
                onClick = { selectedOption = ConsultationOption.USE_DEMO_CONSULTATION },
                testTag = "option_use_demo_consultation"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option C: Capture Clinical Document
            ModalityOptionCard(
                title = "Capture Clinical Document",
                description = "Extract structured clinical data directly from physical lab reports, prescriptions, or referrals using device camera.",
                icon = Icons.Default.CameraAlt,
                isSelected = selectedOption == ConsultationOption.CAPTURE_CLINICAL_DOCUMENT,
                onClick = { selectedOption = ConsultationOption.CAPTURE_CLINICAL_DOCUMENT },
                testTag = "option_capture_document"
            )

            Spacer(modifier = Modifier.weight(1f))

            // Privacy & Safety Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AI-generated information is for clinician review and approval.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModalityOptionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    badge: String? = null,
    testTag: String
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
