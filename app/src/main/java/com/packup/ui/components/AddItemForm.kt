package com.packup.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemForm(
    categories: List<String>,
    onAdd: (name: String, category: String) -> Unit,
    placeholder: String = "Item name...",
    modifier: Modifier = Modifier
) {
    var isOpen by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember(categories) { mutableStateOf(categories.firstOrNull() ?: "General") }
    var categoryExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val handleSubmit = {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            onAdd(trimmed, selectedCategory)
            name = ""
        }
    }

    AnimatedVisibility(
        visible = !isOpen,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        OutlinedButton(
            onClick = { isOpen = true },
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text(" Add item", style = MaterialTheme.typography.labelLarge)
        }
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        ElevatedCard(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(placeholder) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { handleSubmit() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (categories.size > 1) {
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodySmall,
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            selectedCategory = cat
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    FilledTonalButton(
                        onClick = { handleSubmit() },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text(" Add")
                    }

                    TextButton(
                        onClick = { isOpen = false; name = "" }
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
