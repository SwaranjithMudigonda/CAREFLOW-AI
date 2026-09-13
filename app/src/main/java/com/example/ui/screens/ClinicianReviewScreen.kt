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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ActionStatus
import com.example.data.model.FollowUpAction
import com.example.ui.components.CareFlowTopAppBar
import com.example.ui.viewmodel.CareFlowViewModel

@Composable
fun ClinicianReviewScreen(
    viewModel: CareFlowViewModel,
    onBackClick: () -> Unit,
    onApproved: () -> Unit,
    onSavedDraft: () -> Unit
) {
    val consultation by viewModel.currentConsultation.collectAsState()
    val note = consultation?.clinicalNote
    val actions = consultation?.followUpActions ?: emptyList()
    val warnings = consultation?.warnings ?: emptyList()
    val patientSummary = consultation?.patientSummary

    var editingAction by remember { mutableStateOf<FollowUpAction?>(null) }

    Scaffold(
        topBar = {
            CareFlowTopAppBar(
                title = "Review Before Approval",
                subtitle = "Clinician Oversight & Explicit Authorization",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.approveCarePlan()
                            onApproved()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("approve_care_plan_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Approve Care Plan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.saveDraft()
                            onSavedDraft()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_draft_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Draft",
                            fontWeight = FontWeight.SemiBold
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Attending Physician Control Banner
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "CareFlow AI requires your review. Accept, reject, or modify each extracted item before authorizing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Summary of Clinical Note
            if (note != null) {
                item {
                    Text(
                        text = "Clinical Note Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ReviewRow(label = "Chief Complaint", value = "${note.chiefComplaint} (${note.duration})")
                            Spacer(modifier = Modifier.height(6.dp))
                            ReviewRow(label = "Medication", value = note.medications)
                            Spacer(modifier = Modifier.height(6.dp))
                            ReviewRow(label = "Investigation", value = note.investigations)
                            Spacer(modifier = Modifier.height(6.dp))
                            ReviewRow(label = "Assessment", value = note.assessmentPlan.lines().firstOrNull() ?: "")
                        }
                    }
                }
            }

            // Detected Actions Review
            item {
                Text(
                    text = "Follow-up Actions Review (Accept / Edit / Reject)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(actions) { action ->
                ReviewActionCard(
                    action = action,
                    onAccept = { viewModel.updateActionStatus(action.id, ActionStatus.ACCEPTED) },
                    onReject = { viewModel.updateActionStatus(action.id, ActionStatus.REJECTED) },
                    onEdit = { editingAction = action }
                )
            }

            // Safety / AI Warnings Status
            if (warnings.isNotEmpty()) {
                item {
                    Text(
                        text = "Safety & Completeness Checks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                items(warnings) { warning ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (warning.isResolved) Color(0xFFF9FBFB) else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (warning.isResolved) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (warning.isResolved) Icons.Default.Check else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (warning.isResolved) Color(0xFF137333) else MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = warning.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (warning.isResolved && warning.resolutionChoice != null) {
                                    Text(
                                        text = "Status: ${warning.resolutionChoice}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF137333)
                                    )
                                } else {
                                    Text(
                                        text = "Clinician review pending",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Patient Message Preview
            if (patientSummary != null) {
                item {
                    Text(
                        text = "Patient-Facing Message Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "PREVIEW FOR PATIENT:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    letterSpacing = 0.6.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = patientSummary.plainLanguageText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialog for editing an individual action
    if (editingAction != null) {
        EditActionDialog(
            action = editingAction!!,
            onSave = { updatedTiming ->
                viewModel.updateActionTiming(editingAction!!.id, updatedTiming)
                editingAction = null
            },
            onDismiss = { editingAction = null }
        )
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ReviewActionCard(
    action: FollowUpAction,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (action.status) {
                ActionStatus.ACCEPTED -> Color(0xFFF6FBF7)
                ActionStatus.REJECTED -> Color(0xFFFDF7F7)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (action.status) {
                ActionStatus.ACCEPTED -> Color(0xFF137333)
                ActionStatus.REJECTED -> Color(0xFFD32F2F)
                else -> MaterialTheme.colorScheme.outlineVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("review_action_${action.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Current action status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (action.status) {
                                ActionStatus.ACCEPTED -> Color(0xFFE6F4EA)
                                ActionStatus.REJECTED -> Color(0xFFFFEBEE)
                                ActionStatus.MODIFIED -> Color(0xFFE0F2FE)
                                ActionStatus.PENDING_REVIEW -> Color(0xFFFFF3CD)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (action.status) {
                            ActionStatus.ACCEPTED -> "Accepted"
                            ActionStatus.REJECTED -> "Rejected"
                            ActionStatus.MODIFIED -> "Modified"
                            ActionStatus.PENDING_REVIEW -> "Pending"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = when (action.status) {
                            ActionStatus.ACCEPTED -> Color(0xFF137333)
                            ActionStatus.REJECTED -> Color(0xFFC62828)
                            ActionStatus.MODIFIED -> Color(0xFF0369A1)
                            ActionStatus.PENDING_REVIEW -> Color(0xFF856404)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = action.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (action.timing != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Timing: ${action.timing}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clinician Action Buttons: Accept / Edit / Reject
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Accept
                OutlinedButton(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(38.dp).testTag("accept_action_${action.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF137333)
                    )
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Edit
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).height(38.dp).testTag("edit_action_${action.id}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp)
                }

                // Reject
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).height(38.dp).testTag("reject_action_${action.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EditActionDialog(
    action: FollowUpAction,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var timingInput by remember { mutableStateOf(action.timing ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Edit ${action.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = timingInput,
                    onValueChange = { timingInput = it },
                    label = { Text("Clinical Timing / Frequency") },
                    placeholder = { Text("e.g. In 2 weeks, Twice daily") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                        onClick = { onSave(timingInput) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
