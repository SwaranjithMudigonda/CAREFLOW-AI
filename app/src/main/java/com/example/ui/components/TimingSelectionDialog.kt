package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.AIWarning

@Composable
fun TimingSelectionDialog(
    warning: AIWarning,
    onSpecifyTiming: (String) -> Unit,
    onKeepAsIs: () -> Unit,
    onDismissWarning: () -> Unit,
    onCloseDialog: () -> Unit
) {
    var selectedOption by remember { mutableStateOf("In 1 week") }
    var customTiming by remember { mutableStateOf("") }
    val timingOptions = listOf("In 3 days", "In 1 week", "In 2 weeks", "In 1 month", "Custom...")

    Dialog(onDismissRequest = onCloseDialog) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().testTag("timing_selection_dialog")
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Warning Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ACTION REQUIRED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Follow-up Timing Unspecified",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onCloseDialog) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = warning.whyReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = warning.recommendation,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Clinician Follow-up Window:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                timingOptions.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = option }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (selectedOption == option),
                            onClick = { selectedOption = option }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (selectedOption == "Custom...") {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customTiming,
                        onValueChange = { customTiming = it },
                        label = { Text("Specify clinical timeframe") },
                        placeholder = { Text("e.g. 5 days post-laboratory result") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary CTA: Specify Timing
                Button(
                    onClick = {
                        val choice = if (selectedOption == "Custom..." && customTiming.isNotBlank()) {
                            customTiming
                        } else if (selectedOption == "Custom...") {
                            "Custom timeframe"
                        } else {
                            selectedOption
                        }
                        onSpecifyTiming(choice)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("apply_timing_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Specified Timing", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onKeepAsIs,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("keep_as_is_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Keep As Is", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onDismissWarning,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("dismiss_warning_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Dismiss", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
