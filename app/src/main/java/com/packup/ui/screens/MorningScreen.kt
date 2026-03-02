package com.packup.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.packup.data.local.entity.FamilyMemberEntity
import com.packup.data.local.entity.ItemStatus
import com.packup.data.local.entity.MorningItemEntity
import com.packup.data.local.entity.MorningItemStatus
import com.packup.data.local.entity.PackingItemEntity
import com.packup.ui.components.AddItemForm
import com.packup.ui.components.MemberAvatarCircle
import com.packup.ui.components.PackingItemRow
import com.packup.ui.theme.LocalExtendedColors
import com.packup.viewmodel.MemberWithItems

@Composable
fun MorningContent(
    membersWithItems: List<MemberWithItems>,
    morningItems: List<MorningItemEntity>,
    snoozedItems: List<PackingItemEntity>,
    onToggleSnoozedDone: (PackingItemEntity) -> Unit,
    onUnsnooze: (String) -> Unit,
    onToggleMorningDone: (MorningItemEntity) -> Unit,
    onEditMorningItem: (String, String) -> Unit,
    onDeleteMorningItem: (String) -> Unit,
    onAddMorningItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val colorScheme = MaterialTheme.colorScheme

    val snoozedByMember = snoozedItems.groupBy { it.memberId }
    val membersWithSnoozed = membersWithItems
        .filter { snoozedByMember.containsKey(it.member.id) }
        .map { it.member to (snoozedByMember[it.member.id] ?: emptyList()) }

    val morningTodo = morningItems.filter { it.status == MorningItemStatus.TODO }
    val morningDone = morningItems.filter { it.status == MorningItemStatus.DONE }
    val totalCount = snoozedItems.size + morningItems.size

    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(extendedColors.warningContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.WbTwilight,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = extendedColors.onWarningContainer
                )
            }
            Column {
                Text(
                    "Morning of Travel",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface
                )
                Text(
                    "$totalCount item${if (totalCount != 1) "s" else ""} for departure day",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        // Snoozed items grouped by member
        membersWithSnoozed.forEach { (member, items) ->
            SnoozedMemberCard(
                member = member,
                items = items,
                onToggleDone = onToggleSnoozedDone,
                onUnsnooze = onUnsnooze
            )
        }

        // Before leaving items
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(extendedColors.warningContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.WbTwilight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = extendedColors.onWarningContainer
                    )
                }
                Text(
                    "Before Leaving",
                    style = MaterialTheme.typography.titleSmall,
                    color = colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${morningTodo.size} remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))

            var exitingItems by remember { mutableStateOf(setOf<String>()) }

            Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                morningTodo.forEach { item ->
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
                                MorningItemRow(
                                    item = item,
                                    onToggleDone = {
                                        exitingItems = exitingItems + item.id
                                    },
                                    onEdit = { newName -> onEditMorningItem(item.id, newName) },
                                    onDelete = { onDeleteMorningItem(item.id) },
                                    isExiting = isExiting
                                )
                            }
                        }

                        if (isExiting) {
                            LaunchedEffect(item.id) {
                                delay(200)
                                launch { offsetX.animateTo(400f, tween(350)) }
                                launch { itemScale.animateTo(0.92f, tween(350)) }
                                itemAlpha.animateTo(0f, tween(300))
                                delay(50)
                                onToggleMorningDone(item)
                                exitingItems = exitingItems - item.id
                            }
                        }
                    }
                }
                if (morningTodo.isEmpty() && morningDone.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No to-do items yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            if (morningDone.isNotEmpty()) {
                var isDoneExpanded by remember { mutableStateOf(false) }

                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDoneExpanded = !isDoneExpanded }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (isDoneExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = extendedColors.success
                    )
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = extendedColors.success
                    )
                    Text(
                        "Done (${morningDone.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                if (isDoneExpanded) {
                    Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                        morningDone.forEach { item ->
                            key(item.id) {
                                MorningItemRow(
                                    item = item,
                                    onToggleDone = { onToggleMorningDone(item) },
                                    onEdit = { newName -> onEditMorningItem(item.id, newName) },
                                    onDelete = { onDeleteMorningItem(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        AddItemForm(
            categories = listOf("General"),
            onAdd = { name, _ -> onAddMorningItem(name) },
            placeholder = "e.g. Empty fridge, Lock up..."
        )

        if (membersWithSnoozed.isEmpty() && morningItems.isEmpty()) {
            EmptyMorningState()
        }
    }
}

@Composable
private fun SnoozedMemberCard(
    member: FamilyMemberEntity,
    items: List<PackingItemEntity>,
    onToggleDone: (PackingItemEntity) -> Unit,
    onUnsnooze: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = LocalExtendedColors.current

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MemberAvatarCircle(member = member, size = 32)
            Text(
                member.name,
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${items.size} item${if (items.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = extendedColors.onWarningContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(extendedColors.warningContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))

        Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
            items.forEach { item ->
                key(item.id) {
                    PackingItemRow(
                        item = item,
                        onToggleDone = { onToggleDone(item) },
                        onUnsnooze = { onUnsnooze(item.id) },
                        showCategory = true
                    )
                }
            }
        }
    }
}

@Composable
private fun MorningItemRow(
    item: MorningItemEntity,
    onToggleDone: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
    isExiting: Boolean = false
) {
    val asPacking = PackingItemEntity(
        id = item.id,
        name = item.name,
        category = item.category,
        status = if (item.status == MorningItemStatus.DONE) ItemStatus.DONE else ItemStatus.TODO,
        memberId = ""
    )
    PackingItemRow(
        item = asPacking,
        onToggleDone = onToggleDone,
        onEdit = onEdit,
        onDelete = onDelete,
        isExiting = isExiting
    )
}

@Composable
private fun EmptyMorningState() {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surfaceContainerLow)
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Outlined.WbTwilight,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
        Text(
            "No morning items yet",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )
        Text(
            "Swipe items left to snooze them here",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
