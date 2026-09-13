package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClinicalEntity
import com.example.data.model.EntityCategory

@Composable
fun ClinicalEntityChip(
    entity: ClinicalEntity,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor, icon) = when (entity.category) {
        EntityCategory.SYMPTOM -> Quadruple(
            Color(0xFFFDE8E8),
            Color(0xFF9B1C1C),
            Color(0xFFF8B4B4),
            Icons.Default.Healing
        )
        EntityCategory.DURATION -> Quadruple(
            Color(0xFFE1EFFE),
            Color(0xFF1E429F),
            Color(0xFFB4C6FC),
            Icons.Default.AccessTime
        )
        EntityCategory.MEDICATION -> Quadruple(
            Color(0xFFEDF2F7),
            Color(0xFF234E52),
            Color(0xFFCBD5E0),
            Icons.Default.Medication
        )
        EntityCategory.INVESTIGATION -> Quadruple(
            Color(0xFFEBF5FF),
            Color(0xFF1E3A8A),
            Color(0xFFBFDBFE),
            Icons.Default.Biotech
        )
        EntityCategory.FOLLOW_UP -> Quadruple(
            Color(0xFFFEF3C7),
            Color(0xFF92400E),
            Color(0xFFFDE68A),
            Icons.Default.EventRepeat
        )
        EntityCategory.DIAGNOSIS -> Quadruple(
            Color(0xFFE6FFFA),
            Color(0xFF234E52),
            Color(0xFFB2F5EA),
            Icons.Default.Healing
        )
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertidally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = entity.category.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.8f)
                )
                Text(
                    text = entity.text,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private val Alignment.Companion.CenterVertidally: Alignment.Vertical get() = Alignment.CenterVertically
