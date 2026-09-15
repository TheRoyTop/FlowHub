package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GmailAccount
import com.example.ui.FlowViewMode
import com.example.ui.components.WebViewContainer
import com.example.ui.theme.Error
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.Secondary
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SurfaceBright
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.Tertiary
import com.example.ui.theme.TertiaryContainer
import kotlin.math.roundToInt

@Composable
fun FlowScreen(
    mode: FlowViewMode,
    flowWebUrl: String,
    activeAccount: GmailAccount?,
    isRendering: Boolean,
    zoomLevel: Float,
    onModeChange: (FlowViewMode) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    onToggleRender: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceContainerLowest)
    ) {
        // Mode Switcher sub-bar (Canvas Studio vs Official Flow Web)
        Surface(
            color = SurfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeSubButton(
                        text = "Canvas Lumina #408",
                        icon = Icons.Default.ViewInAr,
                        isSelected = mode == FlowViewMode.CANVAS_STUDIO,
                        onClick = { onModeChange(FlowViewMode.CANVAS_STUDIO) },
                        testTag = "btn_flow_canvas"
                    )

                    ModeSubButton(
                        text = "Web Oficial Flow",
                        icon = Icons.Default.Language,
                        isSelected = mode == FlowViewMode.WEB_OFFICIAL,
                        onClick = { onModeChange(FlowViewMode.WEB_OFFICIAL) },
                        testTag = "btn_flow_web"
                    )
                }

                // Status chip with active Gmail tag
                Surface(
                    color = SurfaceContainerHigh,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isRendering) Secondary else Tertiary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (mode == FlowViewMode.WEB_OFFICIAL) {
                                activeAccount?.email?.substringBefore("@") ?: "flow.google"
                            } else if (isRendering) {
                                "Render 60FPS"
                            } else {
                                "FPS: 60"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Main Content Area
        Box(modifier = Modifier.fillMaxSize()) {
            when (mode) {
                FlowViewMode.CANVAS_STUDIO -> {
                    FlowCanvasStudio(
                        isRendering = isRendering,
                        zoomLevel = zoomLevel,
                        activeAccount = activeAccount,
                        onZoomIn = onZoomIn,
                        onZoomOut = onZoomOut,
                        onResetZoom = onResetZoom,
                        onToggleRender = onToggleRender
                    )
                }
                FlowViewMode.WEB_OFFICIAL -> {
                    WebViewContainer(
                        targetUrl = flowWebUrl,
                        activeAccount = activeAccount,
                        showUrlBar = false,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeSubButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = if (isSelected) SurfaceContainerHighest else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (isSelected) Primary.copy(alpha = 0.6f) else OutlineVariant.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FlowCanvasStudio(
    isRendering: Boolean,
    zoomLevel: Float,
    activeAccount: GmailAccount?,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    onToggleRender: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        // High-tech dark grid background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 32.dp.toPx() * zoomLevel
            val startX = (offsetX % step)
            val startY = (offsetY % step)

            var x = startX
            while (x < size.width) {
                drawLine(
                    color = Color(0xFF1B2338),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += step
            }

            var y = startY
            while (y < size.height) {
                drawLine(
                    color = Color(0xFF1B2338),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += step
            }

            // Connection Bézier Curves between pipeline nodes
            val node1Center = Offset(60f + offsetX, 120f + offsetY)
            val node2Center = Offset(240f + offsetX, 260f + offsetY)
            val node3Center = Offset(240f + offsetX, 440f + offsetY)

            val path1 = Path().apply {
                moveTo(node1Center.x + 120f, node1Center.y)
                cubicTo(
                    node1Center.x + 180f, node1Center.y,
                    node2Center.x - 60f, node2Center.y,
                    node2Center.x, node2Center.y
                )
            }
            drawPath(
                path = path1,
                color = Color(0xFF8083FF).copy(alpha = 0.7f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )

            val path2 = Path().apply {
                moveTo(node2Center.x + 120f, node2Center.y)
                cubicTo(
                    node2Center.x + 180f, node2Center.y,
                    node3Center.x - 60f, node3Center.y,
                    node3Center.x, node3Center.y
                )
            }
            drawPath(
                path = path2,
                color = Color(0xFF4EDEA3).copy(alpha = 0.7f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
        }

        // Floating interactive Nodes positioned in Canvas
        Column(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Node 1: Prompt Master
            CanvasNodeCard(
                nodeId = "NODE-01",
                type = "Prompt Principal",
                title = "Cyberpunk Alleyway Neon Rain",
                details = "Lumina Motion V2 • 4K HDR • 60 FPS",
                accentColor = Primary,
                status = "Activo"
            )

            // Node 2: Weight & Seed
            CanvasNodeCard(
                nodeId = "NODE-02",
                type = "Pesos y Modelos",
                title = "Lumina Flow Latent Diffusion",
                details = "Seed #8921 • Guidance 7.5 • Step 35",
                accentColor = Secondary,
                status = if (isRendering) "Generando..." else "Listo"
            )

            // Node 3: Audio & Assembly
            CanvasNodeCard(
                nodeId = "NODE-03",
                type = "Composición & Audio",
                title = "Audio Bed + Timeline Master",
                details = "Sincronía con NotebookLM • Bloque 01",
                accentColor = Tertiary,
                status = "Conectado"
            )
        }

        // Canvas Tool overlay (Zoom controls & Render trigger)
        Surface(
            color = SurfaceContainerHigh.copy(alpha = 0.9f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 80.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onZoomIn, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom +",
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "${(zoomLevel * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(onClick = onZoomOut, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom -",
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onResetZoom, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.FitScreen,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Render Action Pill
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable { onToggleRender() },
            color = if (isRendering) SecondaryContainer else TertiaryContainer,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isRendering) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRendering) "Detener Render" else "Iniciar Render",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CanvasNodeCard(
    nodeId: String,
    type: String,
    title: String,
    details: String,
    accentColor: Color,
    status: String
) {
    Surface(
        modifier = Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(14.dp)),
        color = SurfaceContainerHigh.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = nodeId,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = accentColor
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceContainerHighest)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = type,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = accentColor.copy(alpha = 0.8f)
                )

                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
