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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AIWarning
import com.example.data.model.ActionStatus
import com.example.data.model.FollowUpAction
import com.example.data.model.FollowUpCategory
import com.example.ui.components.CareFlowTopAppBar
import com.example.ui.components.ExplainableAiDialog
import com.example.ui.components.TimingSelectionDialog
import com.example.ui.viewmodel.CareFlowViewModel
import com.example.ui.viewmodel.ExplainableInfo

@Composable
fun FollowUpIntelligenceScreen(
    viewModel: CareFlowViewModel,
    onBackClick: () -> Unit,
    onNavigateToReview: () -> Unit
) {
    val consultation by viewModel.currentConsultation.collectAsState()
    val actions = consultation?.followUpActions ?: emptyList()
    val warnings = consultation?.warnings ?: emptyList()
    val explanationInfo by viewModel.activeExplanation.collectAsState()
    val timingWarning by viewModel.activeTimingWarning.collectAsState()

    Scaffold(
        topBar = {
            CareFlowTopAppBar(
                title = "Follow-up Intelligence",
                subtitle = "Action Extraction & Ambiguity Detection",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 4.dp
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = onNavigateToReview,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("proceed_to_review_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Proceed to Clinician Review",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
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
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Core Differentiator Callout Banner
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
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
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CLINICAL ACTION REASONING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    letterSpacing = 0.6.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "CareFlow detected 3 clinical follow-up actions and identified 1 ambiguity requiring clinician decision.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Ambiguity & Missing Information Warnings
            if (warnings.isNotEmpty()) {
                item {
                    Text(
                        text = "Detected Incomplete or Ambiguous Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                items(warnings) { warning ->
                    WarningCard(
                        warning = warning,
                        onOpenTimingDialog = { viewModel.openTimingDialog(warning) },
                        onKeepAsIs = { viewModel.keepWarningAsIs(warning.id) },
                        onDismiss = { viewModel.dismissWarning(warning.id) },
                        onWhyClick = {
                            viewModel.openExplanation(
                                ExplainableInfo(
                                    title = "Follow-up Timing Flagged",
                                    headline = warning.title,
                                    whyExplanation = warning.whyReason,
                                    sourceContext = warning.warningText,
                                    recommendation = warning.recommendation
                                )
                            )
                        }
                    )
                }
            }

            // Extracted Follow-up Actions Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Extracted Follow-up Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${actions.size} actions found",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(actions) { action ->
                ActionCard(
                    action = action,
                    onWhyClick = {
                        viewModel.openExplanation(
                            ExplainableInfo(
                                title = "${action.title} Detected",
                                headline = action.title,
                                whyExplanation = action.whyReason,
                                sourceContext = action.description
                            )
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Explainable AI Modal Dialog
    if (explanationInfo != null) {
        ExplainableAiDialog(
            info = explanationInfo!!,
            onDismiss = { viewModel.closeExplanation() }
        )
    }

    // Timing Decision Dialog
    if (timingWarning != null) {
        TimingSelectionDialog(
            warning = timingWarning!!,
            onSpecifyTiming = { timing ->
                viewModel.resolveWarningWithTiming(timingWarning!!.id, timing)
            },
            onKeepAsIs = {
                viewModel.keepWarningAsIs(timingWarning!!.id)
            },
            onDismissWarning = {
                viewModel.dismissWarning(timingWarning!!.id)
            },
            onCloseDialog = { viewModel.closeTimingDialog() }
        )
    }
}

@Composable
private fun WarningCard(
    warning: AIWarning,
    onOpenTimingDialog: () -> Unit,
    onKeepAsIs: () -> Unit,
    onDismiss: () -> Unit,
    onWhyClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (warning.isResolved) Color(0xFFF9FBFB) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (warning.isResolved) 1.dp else 1.5.dp,
            if (warning.isResolved) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.tertiary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("ai_warning_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (warning.isResolved) Color(0xFFE6F4EA) else MaterialTheme.colorScheme.tertiaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (warning.isResolved) Icons.Default.Check else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (warning.isResolved) Color(0xFF137333) else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (warning.isResolved) "RESOLVED" else "WARNING: AMBIGUOUS TIMING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            letterSpacing = 0.6.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = if (warning.isResolved) Color(0xFF137333) else MaterialTheme.colorScheme.tertiary
                    )
                }

                // Explainable AI "Why?" Pill Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onWhyClick)
                        .testTag("why_warning_button"),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Why?",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = warning.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = warning.warningText,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = warning.recommendation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (warning.isResolved && warning.resolutionChoice != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF137333),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Decision: ${warning.resolutionChoice}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF137333)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))

                // Explainable AI actions: [Specify Timing], [Keep As Is], [Dismiss]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenTimingDialog,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(42.dp)
                            .testTag("specify_timing_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Specify Timing",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onKeepAsIs,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("keep_as_is_action_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Keep As Is",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.9f)
                            .height(42.dp)
                            .testTag("dismiss_action_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Dismiss",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    action: FollowUpAction,
    onWhyClick: () -> Unit
) {
    val (icon, badgeColor) = when (action.category) {
        FollowUpCategory.MEDICATION -> Pair(Icons.Default.Medication, MaterialTheme.colorScheme.primary)
        FollowUpCategory.INVESTIGATION -> Pair(Icons.Default.Biotech, MaterialTheme.colorScheme.secondary)
        FollowUpCategory.CONSULTATION -> Pair(Icons.Default.Schedule, MaterialTheme.colorScheme.tertiary)
        FollowUpCategory.LIFESTYLE -> Pair(Icons.Default.AutoAwesome, Color(0xFF137333))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("followup_action_${action.id}")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = action.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Explainable AI "Why?" Link
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onWhyClick)
                            .testTag("why_action_${action.id}"),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Why?",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = action.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (action.timing != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Timing: ${action.timing}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
