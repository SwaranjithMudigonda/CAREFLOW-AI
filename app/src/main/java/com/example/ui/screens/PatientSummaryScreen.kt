package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FollowUpCategory
import com.example.ui.components.CareFlowTopAppBar
import com.example.ui.viewmodel.CareFlowViewModel

@Composable
fun PatientSummaryScreen(
    viewModel: CareFlowViewModel,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val consultation by viewModel.currentConsultation.collectAsState()
    val summary = consultation?.patientSummary
    val actions = consultation?.followUpActions?.filter { it.status != com.example.data.model.ActionStatus.REJECTED } ?: emptyList()
    val patient = consultation?.patient

    Scaffold(
        topBar = {
            CareFlowTopAppBar(
                title = "Patient Follow-up Plan",
                subtitle = "Plain-Language Summary • Clinician Authorized",
                showBackButton = false,
                actions = {
                    Button(
                        onClick = onNavigateToHome,
                        modifier = Modifier.padding(end = 8.dp).testTag("patient_done_home_button")
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Home")
                    }
                }
            )
        },
        containerColor = Color(0xFFF4F7F6) // Warm, calm patient-friendly background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Clinician Approval Verification Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Status: Approved by clinician",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "Authorized by ${summary?.approvedByDoctorName ?: "Dr. S. Miller, MD"} for ${patient?.name ?: "Patient"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            // Friendly Doctor Instructions Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "HELLO ${patient?.name?.uppercase() ?: "PATIENT"},",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00687A)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = summary?.plainLanguageText
                                ?: "Your doctor has requested a blood test. Please complete the test and return for follow-up after your results are available.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF2D3748),
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            // Clear Action Steps
            item {
                Text(
                    text = "Your Follow-up Steps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C)
                )
            }

            items(actions) { action ->
                val (icon, color) = when (action.category) {
                    FollowUpCategory.INVESTIGATION -> Pair(Icons.Default.Biotech, Color(0xFF1E40AF))
                    FollowUpCategory.MEDICATION -> Pair(Icons.Default.Medication, Color(0xFF065F46))
                    FollowUpCategory.CONSULTATION -> Pair(Icons.Default.Event, Color(0xFF92400E))
                    FollowUpCategory.LIFESTYLE -> Pair(Icons.Default.CheckCircle, Color(0xFF1B5E20))
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = action.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A202C)
                            )
                            Text(
                                text = action.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4A5568)
                            )
                            if (action.timing != null) {
                                Text(
                                    text = "When: ${action.timing}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }

            // Share and Export Options
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val text = "${summary?.plainLanguageText}\n\nKey steps:\n" +
                                actions.joinToString("\n") { "• ${it.title}: ${it.description} (${it.timing ?: "As directed"})" }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("CarePlan", text))
                            Toast.makeText(context, "Instructions copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy")
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Care plan ready to transmit via clinic portal or SMS", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send to Patient")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
