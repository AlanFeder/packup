package com.packup.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.packup.data.local.entity.CategoryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerSheet(
    categories: List<CategoryEntity>,
    onAddCategory: (String, String) -> Unit,
    onRenameCategory: (String, String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onSetCategoryIcon: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        CategoryManagerContent(
            categories = categories,
            onAddCategory = onAddCategory,
            onRenameCategory = onRenameCategory,
            onDeleteCategory = onDeleteCategory,
            onSetCategoryIcon = onSetCategoryIcon,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerButton(
    categories: List<CategoryEntity>,
    onAddCategory: (String, String) -> Unit,
    onRenameCategory: (String, String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onSetCategoryIcon: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }

    TextButton(
        onClick = { showSheet = true },
        modifier = modifier
    ) {
        Icon(Icons.AutoMirrored.Outlined.Label, contentDescription = null, modifier = Modifier.size(16.dp))
        Text(" Manage categories", style = MaterialTheme.typography.labelMedium)
    }

    if (showSheet) {
        CategoryManagerSheet(
            categories = categories,
            onAddCategory = onAddCategory,
            onRenameCategory = onRenameCategory,
            onDeleteCategory = onDeleteCategory,
            onSetCategoryIcon = onSetCategoryIcon,
            onDismiss = { showSheet = false }
        )
    }
}

@Composable
private fun CategoryManagerContent(
    categories: List<CategoryEntity>,
    onAddCategory: (String, String) -> Unit,
    onRenameCategory: (String, String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onSetCategoryIcon: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var newCatName by remember { mutableStateOf("") }
    var newCatIcon by remember { mutableStateOf("\uD83D\uDCE6") }
    var editingCat by remember { mutableStateOf<String?>(null) }
    var editValue by remember { mutableStateOf("") }
    var pickingIconFor by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.Label,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                "Categories",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f).padding(start = 12.dp)
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Outlined.Close, "Close")
            }
        }

        HorizontalDivider()

        Column(
            modifier = Modifier
                .heightIn(max = 320.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            categories.forEach { cat ->
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (editingCat == cat.name) {
                            BasicTextField(
                                value = editValue,
                                onValueChange = { editValue = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(colorScheme.primary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    val trimmed = editValue.trim()
                                    if (trimmed.isNotEmpty() && trimmed != cat.name && categories.none { it.name == trimmed }) {
                                        onRenameCategory(cat.name, trimmed)
                                    }
                                    editingCat = null
                                }),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colorScheme.surfaceContainerHighest)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                            IconButton(
                                onClick = {
                                    val trimmed = editValue.trim()
                                    if (trimmed.isNotEmpty() && trimmed != cat.name && categories.none { it.name == trimmed }) {
                                        onRenameCategory(cat.name, trimmed)
                                    }
                                    editingCat = null
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Check, "Save", modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { editingCat = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Outlined.Close, "Cancel", modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primaryContainer)
                                    .then(
                                        if (pickingIconFor == cat.name)
                                            Modifier.border(2.dp, colorScheme.primary, CircleShape)
                                        else Modifier
                                    )
                                    .clickable {
                                        pickingIconFor = if (pickingIconFor == cat.name) null else cat.name
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                CategoryIconContent(cat.iconKey, 14)
                            }
                            Text(
                                cat.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    editingCat = cat.name
                                    editValue = cat.name
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Outlined.Edit, "Rename", modifier = Modifier.size(14.dp),
                                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            }
                            IconButton(
                                onClick = {
                                    if (confirmDelete == cat.name) {
                                        onDeleteCategory(cat.name)
                                        confirmDelete = null
                                    } else {
                                        confirmDelete = cat.name
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Outlined.Close, "Delete", modifier = Modifier.size(14.dp),
                                    tint = if (confirmDelete == cat.name) colorScheme.error
                                    else colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = pickingIconFor == cat.name,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        EmojiPickerField(
                            currentEmoji = cat.iconKey,
                            onSelect = { emoji ->
                                onSetCategoryIcon(cat.name, emoji)
                                pickingIconFor = null
                            }
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colorScheme.surfaceContainerHigh)
                    .then(
                        if (pickingIconFor == "__new__")
                            Modifier.border(2.dp, colorScheme.primary, CircleShape)
                        else Modifier
                    )
                    .clickable {
                        pickingIconFor = if (pickingIconFor == "__new__") null else "__new__"
                    },
                contentAlignment = Alignment.Center
            ) {
                CategoryIconContent(newCatIcon, 16)
            }
            OutlinedTextField(
                value = newCatName,
                onValueChange = { newCatName = it },
                placeholder = { Text("New category...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodySmall
            )
            FilledTonalButton(
                onClick = {
                    val trimmed = newCatName.trim()
                    if (trimmed.isNotEmpty() && categories.none { it.name == trimmed }) {
                        onAddCategory(trimmed, newCatIcon)
                        newCatName = ""
                        newCatIcon = "\uD83D\uDCE6"
                    }
                },
                enabled = newCatName.isNotBlank() && categories.none { it.name == newCatName.trim() },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Add")
            }
        }

        AnimatedVisibility(
            visible = pickingIconFor == "__new__",
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            EmojiPickerField(
                currentEmoji = newCatIcon,
                onSelect = { emoji ->
                    newCatIcon = emoji
                    pickingIconFor = null
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun CategoryIconContent(iconKey: String, sizeDp: Int) {
    val colorScheme = MaterialTheme.colorScheme
    if (CategoryIcons.isEmoji(iconKey)) {
        Text(iconKey, fontSize = sizeDp.sp, textAlign = TextAlign.Center)
    } else {
        Icon(
            CategoryIcons.getIcon(iconKey),
            contentDescription = null,
            modifier = Modifier.size(sizeDp.dp),
            tint = colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun EmojiPickerField(
    currentEmoji: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue("", TextRange.Zero))
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Pick an emoji:", style = MaterialTheme.typography.labelMedium, color = colorScheme.onSurfaceVariant)
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                val newText = newValue.text
                if (newText.isNotEmpty()) {
                    val lastCodePoint = newText.codePointBefore(newText.length)
                    val start = newText.length - Character.charCount(lastCodePoint)
                    val emoji = newText.substring(start)
                    onSelect(emoji)
                    textFieldValue = TextFieldValue("", TextRange.Zero)
                } else {
                    textFieldValue = newValue
                }
            },
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            cursorBrush = SolidColor(colorScheme.primary),
            modifier = Modifier
                .width(64.dp)
                .focusRequester(focusRequester)
                .clip(RoundedCornerShape(8.dp))
                .background(colorScheme.surfaceContainerHighest)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )
    }
}
