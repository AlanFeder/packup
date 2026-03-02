package com.packup.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.packup.ui.theme.LocalExtendedColors
import com.packup.viewmodel.MemberWithItems
import java.io.File

@Composable
fun ProgressHeader(
    memberWithItems: MemberWithItems,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val colorScheme = MaterialTheme.colorScheme
    val member = memberWithItems.member
    val total = memberWithItems.totalCount
    val done = memberWithItems.doneCount
    val snoozed = memberWithItems.snoozedCount
    val remaining = memberWithItems.remaining
    val allDone = memberWithItems.allDone
    val progress = if (total > 0) memberWithItems.handledCount.toFloat() / total else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(700),
        label = "progress"
    )

    val ringColor = if (allDone) extendedColors.success else colorScheme.primary
    val trackColor = colorScheme.outlineVariant.copy(alpha = 0.3f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Circular progress with avatar
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .drawBehind {
                        val strokeWidth = 3.5.dp.toPx()
                        val padding = strokeWidth / 2
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx()),
                            topLeft = Offset(padding, padding),
                            size = arcSize
                        )
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = Offset(padding, padding),
                            size = arcSize
                        )
                    }
            )

            if (member.photoUri.isNotEmpty()) {
                AsyncImage(
                    model = File(member.photoUri),
                    contentDescription = member.name,
                    modifier = Modifier.size(50.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else if (member.iconKey.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        MemberIcons.getIcon(member.iconKey),
                        contentDescription = member.name,
                        modifier = Modifier.size(24.dp),
                        tint = colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.avatar,
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            if (allDone) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .size(24.dp)
                        .background(extendedColors.success, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "All done",
                        modifier = Modifier.size(16.dp),
                        tint = extendedColors.onSuccess
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (allDone) "All packed!" else member.name,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface
            )
            Text(
                text = if (allDone) "Everything is packed or snoozed to morning"
                else buildString {
                    append("$done packed")
                    if (snoozed > 0) append(" · $snoozed morning")
                    append(" · $remaining left")
                },
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            if (!allDone) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(CircleShape),
                    color = colorScheme.primary,
                    trackColor = colorScheme.surfaceContainerHighest,
                )
            }
        }
    }
}
