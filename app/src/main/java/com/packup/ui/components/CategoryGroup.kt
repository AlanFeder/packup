package com.packup.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.packup.data.local.entity.CategoryEntity
import com.packup.data.local.entity.ItemStatus
import com.packup.data.local.entity.PackingItemEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CategoryGroup(
    category: String,
    categoryEntity: CategoryEntity?,
    items: List<PackingItemEntity>,
    onToggleDone: (PackingItemEntity) -> Unit,
    onSnooze: (String) -> Unit,
    onEdit: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onMarkAllDone: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val iconKey = categoryEntity?.iconKey ?: "package"
    val isEmoji = CategoryIcons.isEmoji(iconKey)

    val todoItems = items.filter { it.status == ItemStatus.TODO }

    if (todoItems.isEmpty()) return

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isEmoji) {
                    Text(iconKey, style = MaterialTheme.typography.titleSmall)
                } else {
                    Icon(
                        CategoryIcons.getIcon(iconKey),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = colorScheme.onPrimaryContainer
                    )
                }
            }
            Text(
                text = category,
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (todoItems.isNotEmpty()) {
                TextButton(
                    onClick = { onMarkAllDone(category) },
                ) {
                    Icon(
                        Icons.Outlined.DoneAll,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        " All done",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Items
        var exitingItems by remember { mutableStateOf(setOf<String>()) }

        Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
            todoItems.forEach { item ->
                key(item.id) {
                    val isExiting = item.id in exitingItems
                    val offsetX = remember { Animatable(0f) }
                    val itemAlpha = remember { Animatable(1f) }
                    val itemScale = remember { Animatable(1f) }

                    AnimatedVisibility(
                        visible = !isExiting || offsetX.isRunning || itemAlpha.value > 0f,
                        exit = shrinkVertically(tween(250))
                    ) {
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(offsetX.value.toInt(), 0) }
                                .alpha(itemAlpha.value)
                                .scale(itemScale.value)
                        ) {
                            PackingItemRow(
                                item = item,
                                onToggleDone = {
                                    exitingItems = exitingItems + item.id
                                },
                                onSnooze = { onSnooze(item.id) },
                                onEdit = { newName -> onEdit(item.id, newName) },
                                onDelete = { onDelete(item.id) },
                                isExiting = isExiting
                            )
                        }
                    }

                    if (isExiting) {
                        LaunchedEffect(item.id) {
                            // Phase 1: brief pause to let the checkbox fill
                            delay(200)
                            // Phase 2: slide right, shrink, and fade
                            launch { offsetX.animateTo(400f, tween(350)) }
                            launch { itemScale.animateTo(0.92f, tween(350)) }
                            itemAlpha.animateTo(0f, tween(300))
                            delay(50)
                            onToggleDone(item)
                            exitingItems = exitingItems - item.id
                        }
                    }
                }
            }
        }
    }
}
