package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainTab
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.Tertiary

@Composable
fun MainSegmentedTabs(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        color = SurfaceContainerLowest,
        modifier = modifier
            .fillMaxWidth()
            .testTag("main_segmented_tabs")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabPill(
                title = "Flow",
                icon = Icons.Default.Hub,
                isSelected = selectedTab == MainTab.FLOW,
                badge = "Live",
                badgeColor = Tertiary,
                onClick = { onTabSelected(MainTab.FLOW) },
                testTag = "tab_flow"
            )

            TabPill(
                title = "NotebookLM",
                icon = Icons.Default.AutoAwesome,
                isSelected = selectedTab == MainTab.NOTEBOOK,
                badge = "14 Docs",
                badgeColor = Primary,
                onClick = { onTabSelected(MainTab.NOTEBOOK) },
                testTag = "tab_notebook"
            )

            TabPill(
                title = "Assets & Continuidad",
                icon = Icons.Default.Folder,
                isSelected = selectedTab == MainTab.ASSETS,
                badge = "38",
                badgeColor = null,
                onClick = { onTabSelected(MainTab.ASSETS) },
                testTag = "tab_assets"
            )

            // Add Tab Affordance
            Surface(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable { /* Could create a custom tab */ },
                color = SurfaceContainer,
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nuevo espacio",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TabPill(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    badge: String?,
    badgeColor: Color?,
    onClick: () -> Unit,
    testTag: String
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) SurfaceContainerHigh else SurfaceContainer,
        label = "tab_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Primary.copy(alpha = 0.7f) else OutlineVariant.copy(alpha = 0.3f),
        label = "tab_border"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tab_content"
    )

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
            )

            if (badge != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            (badgeColor ?: MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.18f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = badgeColor ?: MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
