package com.rifqi.industrialweighbridge.presentation.components.weighing

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rifqi.industrialweighbridge.presentation.utils.WeightFormatter

// Color Constants
private val StableGreen = Color(0xFF4CAF50)
private val UnstableYellow = Color(0xFFFFC107)

@Composable
fun DigitalWeightDisplay(
    weight: Double,
    isStable: Boolean,
    isManualMode: Boolean,
    onManualModeToggle: () -> Unit,
    onWeightChange: (Double) -> Unit
) {
    var weightInput by remember { mutableStateOf("") }

    // Animation for unstable indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by
    infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            ),
        label = "pulseAlpha"
    )

    val statusColor by
    animateColorAsState(
        targetValue = if (isStable) StableGreen else UnstableYellow,
        animationSpec = tween(300),
        label = "statusColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TIMBANGAN DIGITAL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isManualMode) "MANUAL" else "AUTO",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(checked = isManualMode, onCheckedChange = { onManualModeToggle() })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Digital Display - Seven Segment Style
                if (isManualMode) {
                    // Manual input mode
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { newValue ->
                            weightInput = WeightFormatter.formatInputAsYouType(newValue)
                            WeightFormatter.parseWeight(weightInput)?.let { onWeightChange(it) }
                        },
                        label = { Text("Masukkan Berat") },
                        placeholder = { Text("Contoh: 12.345,67") },
                        suffix = { Text("kg", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle =
                            MaterialTheme.typography.headlineMedium.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                    )
                } else {
                    // Digital display mode
                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = 0.1f
                                    )
                                ) // Light background for contrast
                                .border(
                                    width = 3.dp,
                                    color =
                                        if (isStable) StableGreen
                                        else
                                            UnstableYellow.copy(
                                                alpha = pulseAlpha
                                            ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Weight value - Digital style
                            Text(
                                text = WeightFormatter.formatNumber(weight),
                                style =
                                    MaterialTheme.typography.displayLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 56.sp,
                                        letterSpacing = 4.sp
                                    ),
                                color = MaterialTheme.colorScheme.onSurface // Better contrast
                            )

                            // Unit
                            Text(
                                text = "kg",
                                style =
                                    MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Bar
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(statusColor.copy(alpha = 0.2f))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier =
                                Modifier.size(12.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                                    .alpha(if (isStable) 1f else pulseAlpha)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector =
                                if (isStable) Icons.Default.CheckCircle
                                else Icons.Default.Scale,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text =
                                if (isStable) "BERAT STABIL - SIAP SIMPAN"
                                else "Menunggu berat stabil...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }
        }
    }
}
