package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ClinicalNote
import com.example.ui.components.CareFlowTopAppBar
import com.example.ui.components.ClinicalEntityChip
import com.example.ui.viewmodel.CareFlowViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClinicalNoteScreen(
    viewModel: CareFlowViewModel,
    onBackClick: () -> Unit,
    onContinueToFollowUp: () -> Unit
) {
    val consultation by viewModel.currentConsultation.collectAsState()
    val note = consultation?.clinicalNote ?: ClinicalNote()
    val entities = consultation?.clinicalEntities ?: emptyList()

    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CareFlowTopAppBar(
                title = "Structured Clinical Note",
                subtitle = "${consultation?.patient?.name ?: "Patient"} • AI-Structured Draft",
                showBackButton = true,
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.testTag("edit_clinical_note_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Clinical Note",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("edit_note_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit Note",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onContinueToFollowUp,
                        modifier = Modifier
                            .weight(2f)
                            .height(52.dp)
                            .testTag("continue_to_followup_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Continue to Follow-up Intelligence",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            // Extracted Clinical Entities Chips Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Extracted Clinical Entities",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            entities.forEach { entity ->
                                ClinicalEntityChip(entity = entity)
                            }
                        }
                    }
                }
            }

            // Structured Clinical Note Sections
            item {
                Text(
                    text = "Structured Medical Documentation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                NoteSectionCard(
                    title = "Chief Complaint",
                    body = note.chiefComplaint,
                    secondary = "Duration: ${note.duration}"
                )
            }

            item {
                NoteSectionCard(
                    title = "Symptoms",
                    body = note.symptoms
                )
            }

            item {
                NoteSectionCard(
                    title = "Relevant History",
                    body = note.relevantHistory
                )
            }

            item {
                NoteSectionCard(
                    title = "Medications",
                    body = note.medications,
                    highlightColor = MaterialTheme.colorScheme.primary
                )
            }

            item {
                NoteSectionCard(
                    title = "Investigations",
                    body = note.investigations,
                    highlightColor = MaterialTheme.colorScheme.secondary
                )
            }

            item {
                NoteSectionCard(
                    title = "Assessment & Plan",
                    body = note.assessmentPlan
                )
            }

            item {
                NoteSectionCard(
                    title = "Follow-up Recommendation",
                    body = note.followUp,
                    highlightColor = MaterialTheme.colorScheme.tertiary
                )
            }

            item {
                // Safety notice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Clinician modification allowed at all stages before care plan approval.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Edit Clinical Note Dialog
    if (showEditDialog) {
        EditClinicalNoteDialog(
            initialNote = note,
            onSave = { updated ->
                viewModel.updateClinicalNote(updated)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
private fun NoteSectionCard(
    title: String,
    body: String,
    secondary: String? = null,
    highlightColor: Color? = null
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 0.6.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = highlightColor ?: MaterialTheme.colorScheme.primary
                )
                if (secondary != null) {
                    Text(
                        text = secondary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = body.ifBlank { "None documented" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun EditClinicalNoteDialog(
    initialNote: ClinicalNote,
    onSave: (ClinicalNote) -> Unit,
    onDismiss: () -> Unit
) {
    var chiefComplaint by remember { mutableStateOf(initialNote.chiefComplaint) }
    var duration by remember { mutableStateOf(initialNote.duration) }
    var symptoms by remember { mutableStateOf(initialNote.symptoms) }
    var medications by remember { mutableStateOf(initialNote.medications) }
    var investigations by remember { mutableStateOf(initialNote.investigations) }
    var assessmentPlan by remember { mutableStateOf(initialNote.assessmentPlan) }
    var followUp by remember { mutableStateOf(initialNote.followUp) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(modifier = Modifier.padding(20.dp)) {
                item {
                    Text(
                        text = "Edit Structured Clinical Note",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                item {
                    OutlinedTextField(
                        value = chiefComplaint,
                        onValueChange = { chiefComplaint = it },
                        label = { Text("Chief Complaint") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = symptoms,
                        onValueChange = { symptoms = it },
                        label = { Text("Symptoms") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = medications,
                        onValueChange = { medications = it },
                        label = { Text("Medications") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = investigations,
                        onValueChange = { investigations = it },
                        label = { Text("Investigations") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = assessmentPlan,
                        onValueChange = { assessmentPlan = it },
                        label = { Text("Assessment / Plan") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = followUp,
                        onValueChange = { followUp = it },
                        label = { Text("Follow-up") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                onSave(
                                    initialNote.copy(
                                        chiefComplaint = chiefComplaint,
                                        duration = duration,
                                        symptoms = symptoms,
                                        medications = medications,
                                        investigations = investigations,
                                        assessmentPlan = assessmentPlan,
                                        followUp = followUp
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}
