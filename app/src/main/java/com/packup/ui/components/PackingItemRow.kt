package com.packup.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.packup.data.local.entity.ItemStatus
import com.packup.data.local.entity.PackingItemEntity
import com.packup.ui.theme.LocalExtendedColors

@Composable
fun PackingItemRow(
    item: PackingItemEntity,
    onToggleDone: () -> Unit,
    onSnooze: (() -> Unit)? = null,
    onUnsnooze: (() -> Unit)? = null,
    onEdit: ((String) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    showCategory: Boolean = false,
    showOwner: String? = null,
    isExiting: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDone = item.status == ItemStatus.DONE || isExiting
    val isSnoozed = item.status == ItemStatus.SNOOZED
    val canSwipe = !isDone && !isSnoozed && onSnooze != null

    var isEditing by remember { mutableStateOf(false) }
    var editValue by remember(item.name) { mutableStateOf(item.name) }
    var confirmDelete by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    if (canSwipe) {
        SwipeableItem(
            item = item,
            isDone = isDone,
            isExiting = isExiting,
            isEditing = isEditing,
            editValue = editValue,
            confirmDelete = confirmDelete,
            focusRequester = focusRequester,
            showCategory = showCategory,
            showOwner = showOwner,
            onToggleDone = onToggleDone,
            onSnooze = onSnooze!!,
            onUnsnooze = onUnsnooze,
            onEdit = onEdit,
            onDelete = onDelete,
            onStartEdit = {
                editValue = item.name
                isEditing = true
            },
            onEditValueChange = { editValue = it },
            onSaveEdit = {
                val trimmed = editValue.trim()
                if (trimmed.isNotEmpty() && trimmed != item.name) onEdit?.invoke(trimmed)
                isEditing = false
            },
            onCancelEdit = {
                editValue = item.name
                isEditing = false
            },
            onConfirmDeleteChange = { confirmDelete = it },
            modifier = modifier
        )
    } else {
        ItemContent(
            item = item,
            isDone = isDone,
            isSnoozed = isSnoozed,
            isExiting = isExiting,
            isEditing = isEditing,
            editValue = editValue,
            confirmDelete = confirmDelete,
            focusRequester = focusRequester,
            showCategory = showCategory,
            showOwner = showOwner,
            onToggleDone = onToggleDone,
            onUnsnooze = onUnsnooze,
            onEdit = onEdit,
            onDelete = onDelete,
            onStartEdit = {
                editValue = item.name
                isEditing = true
            },
            onEditValueChange = { editValue = it },
            onSaveEdit = {
                val trimmed = editValue.trim()
                if (trimmed.isNotEmpty() && trimmed != item.name) onEdit?.invoke(trimmed)
                isEditing = false
            },
            onCancelEdit = {
                editValue = item.name
                isEditing = false
            },
            onConfirmDeleteChange = { confirmDelete = it },
            modifier = modifier
        )
    }
}

@Composable
private fun SwipeableItem(
    item: PackingItemEntity,
    isDone: Boolean,
    isExiting: Boolean,
    isEditing: Boolean,
    editValue: String,
    confirmDelete: Boolean,
    focusRequester: FocusRequester,
    showCategory: Boolean,
    showOwner: String?,
    onToggleDone: () -> Unit,
    onSnooze: () -> Unit,
    onUnsnooze: (() -> Unit)?,
    onEdit: ((String) -> Unit)?,
    onDelete: (() -> Unit)?,
    onStartEdit: () -> Unit,
    onEditValueChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onConfirmDeleteChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSnooze()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    extendedColors.warningContainer
                else extendedColors.warningContainer.copy(alpha = 0.3f),
                label = "swipeBg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.WbTwilight,
                        contentDescription = "Snooze to morning",
                        modifier = Modifier.size(18.dp),
                        tint = extendedColors.onWarningContainer
                    )
                    Text(
                        "Morning",
                        style = MaterialTheme.typography.labelMedium,
                        color = extendedColors.onWarningContainer
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
        modifier = modifier
    ) {
        ItemContent(
            item = item,
            isDone = isDone,
            isSnoozed = false,
            isExiting = isExiting,
            isEditing = isEditing,
            editValue = editValue,
            confirmDelete = confirmDelete,
            focusRequester = focusRequester,
            showCategory = showCategory,
            showOwner = showOwner,
            onToggleDone = onToggleDone,
            onUnsnooze = onUnsnooze,
            onEdit = onEdit,
            onDelete = onDelete,
            onStartEdit = onStartEdit,
            onEditValueChange = onEditValueChange,
            onSaveEdit = onSaveEdit,
            onCancelEdit = onCancelEdit,
            onConfirmDeleteChange = onConfirmDeleteChange,
        )
    }
}

@Composable
private fun ItemContent(
    item: PackingItemEntity,
    isDone: Boolean,
    isSnoozed: Boolean,
    isExiting: Boolean = false,
    isEditing: Boolean,
    editValue: String,
    confirmDelete: Boolean,
    focusRequester: FocusRequester,
    showCategory: Boolean,
    showOwner: String?,
    onToggleDone: () -> Unit,
    onUnsnooze: (() -> Unit)?,
    onEdit: ((String) -> Unit)?,
    onDelete: (() -> Unit)?,
    onStartEdit: () -> Unit,
    onEditValueChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onConfirmDeleteChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = LocalExtendedColors.current
    val elevation by animateDpAsState(
        targetValue = if (isDone) 0.dp else 1.dp,
        label = "elevation"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isExiting -> extendedColors.successContainer.copy(alpha = 0.4f)
            else -> colorScheme.surfaceContainerLow
        },
        animationSpec = tween(200),
        label = "rowBg"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .then(if (isDone && !isExiting) Modifier.alpha(0.55f) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isDone,
            onCheckedChange = { onToggleDone() },
            colors = CheckboxDefaults.colors(
                checkedColor = extendedColors.success,
                uncheckedColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                checkmarkColor = extendedColors.onSuccess
            )
        )

        if (isEditing) {
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
            BasicTextField(
                value = editValue,
                onValueChange = onEditValueChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = colorScheme.onSurface
                ),
                singleLine = true,
                cursorBrush = SolidColor(colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSaveEdit() }),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorScheme.surfaceContainerHighest)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
            IconButton(onClick = onSaveEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Check, "Save", tint = extendedColors.success, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onCancelEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.Close, "Cancel", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        } else {
            Column(
                modifier = Modifier.weight(1f).padding(vertical = 4.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (isDone) colorScheme.onSurfaceVariant else colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showCategory || showOwner != null) {
                    Text(
                        text = buildString {
                            showOwner?.let { append(it) }
                            if (showOwner != null && showCategory) append(" · ")
                            if (showCategory) append(item.category)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            if (isSnoozed && onUnsnooze != null) {
                IconButton(onClick = onUnsnooze, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.Undo, "Move back", tint = colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
            if (onEdit != null) {
                IconButton(
                    onClick = onStartEdit,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(Icons.Outlined.Edit, "Edit", modifier = Modifier.size(16.dp))
                }
            }
            if (onDelete != null) {
                IconButton(
                    onClick = {
                        if (confirmDelete) {
                            onDelete()
                            onConfirmDeleteChange(false)
                        } else {
                            onConfirmDeleteChange(true)
                        }
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (confirmDelete) colorScheme.error
                        else colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(Icons.Outlined.Delete, "Delete", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
