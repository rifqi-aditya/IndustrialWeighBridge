package com.rifqi.industrialweighbridge.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.MouseInfo
import java.awt.Window

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WindowTitleBar(
        window: Window,
        onMinimize: () -> Unit,
        onMaximize: () -> Unit,
        onClose: () -> Unit,
        modifier: Modifier = Modifier
) {
        Surface(
                modifier = modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
        ) {
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(48.dp)
                                        .pointerInput(Unit) {
                                                var startX = 0
                                                var startY = 0
                                                detectDragGestures(
                                                        onDragStart = {
                                                                val point =
                                                                        MouseInfo.getPointerInfo()
                                                                                .location
                                                                startX = point.x - window.x
                                                                startY = point.y - window.y
                                                        },
                                                        onDrag = { change, _ ->
                                                                change.consume()
                                                                val point =
                                                                        MouseInfo.getPointerInfo()
                                                                                .location
                                                                window.setLocation(
                                                                        point.x - startX,
                                                                        point.y - startY
                                                                )
                                                        }
                                                )
                                        }
                                        .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                        // Left: Logo and Title
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                // App Icon
                                Box(
                                        modifier =
                                                Modifier.size(32.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                                MaterialTheme.colorScheme.primary
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Scale,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(20.dp)
                                        )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // App Title
                                Text(
                                        text = "Industrial WeighBridge",
                                        style =
                                                MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        letterSpacing = 0.5.sp
                                                ),
                                        color = MaterialTheme.colorScheme.onSurface
                                )
                        }

                        // Right: Window Controls
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                // Minimize - horizontal line
                                IconButton(onClick = onMinimize, modifier = Modifier.size(40.dp)) {
                                        Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Minimize",
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                }

                                // Maximize - square outline
                                IconButton(onClick = onMaximize, modifier = Modifier.size(40.dp)) {
                                        Icon(
                                                imageVector = Icons.Default.CropSquare,
                                                contentDescription = "Maximize",
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                }

                                // Close - X
                                IconButton(onClick = onClose, modifier = Modifier.size(40.dp)) {
                                        Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close",
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.error
                                        )
                                }
                        }
                }
        }
}
