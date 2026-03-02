package com.packup.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.packup.data.local.entity.FamilyMemberEntity
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberManagerSheet(
    members: List<FamilyMemberEntity>,
    onAddMember: (String, String) -> Unit,
    onRenameMember: (String, String) -> Unit,
    onDeleteMember: (String) -> Unit,
    onSetMemberIcon: (String, String) -> Unit,
    onSetMemberPhoto: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        MemberManagerContent(
            members = members,
            onAddMember = onAddMember,
            onRenameMember = onRenameMember,
            onDeleteMember = onDeleteMember,
            onSetMemberIcon = onSetMemberIcon,
            onSetMemberPhoto = onSetMemberPhoto,
            onDismiss = onDismiss
        )
    }
}

private fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val dir = File(context.filesDir, "member_photos")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.absolutePath
    } catch (_: Exception) {
        null
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MemberManagerContent(
    members: List<FamilyMemberEntity>,
    onAddMember: (String, String) -> Unit,
    onRenameMember: (String, String) -> Unit,
    onDeleteMember: (String) -> Unit,
    onSetMemberIcon: (String, String) -> Unit,
    onSetMemberPhoto: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var newMemberName by remember { mutableStateOf("") }
    var newMemberIcon by remember { mutableStateOf("person") }
    var editingMemberId by remember { mutableStateOf<String?>(null) }
    var editValue by remember { mutableStateOf("") }
    var pickingIconFor by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf<String?>(null) }
    var pickingPhotoForMemberId by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && pickingPhotoForMemberId != null) {
            val internalPath = copyImageToInternalStorage(context, uri)
            if (internalPath != null) {
                onSetMemberPhoto(pickingPhotoForMemberId!!, internalPath)
            }
        }
        pickingPhotoForMemberId = null
    }

    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.People,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                "Family Members",
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
                .heightIn(max = 400.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            members.forEach { member ->
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (editingMemberId == member.id) {
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
                                    if (trimmed.isNotEmpty() && trimmed != member.name) {
                                        onRenameMember(member.id, trimmed)
                                    }
                                    editingMemberId = null
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
                                    if (trimmed.isNotEmpty() && trimmed != member.name) {
                                        onRenameMember(member.id, trimmed)
                                    }
                                    editingMemberId = null
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Check, "Save", modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { editingMemberId = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Outlined.Close, "Cancel", modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable {
                                        pickingPhotoForMemberId = member.id
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                MemberAvatarCircle(
                                    member = member,
                                    size = 36,
                                    showCameraBadge = true
                                )
                            }
                            Text(
                                member.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    editingMemberId = member.id
                                    editValue = member.name
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Edit, "Rename",
                                    modifier = Modifier.size(14.dp),
                                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (confirmDelete == member.id) {
                                        onDeleteMember(member.id)
                                        confirmDelete = null
                                    } else {
                                        confirmDelete = member.id
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Close, "Delete",
                                    modifier = Modifier.size(14.dp),
                                    tint = if (confirmDelete == member.id) colorScheme.error
                                    else colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = pickingIconFor == member.id,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        MemberIconPickerRow(
                            selectedKey = member.iconKey,
                            onSelect = { key ->
                                onSetMemberIcon(member.id, key)
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
                        if (pickingIconFor == "__new_member__")
                            Modifier.border(2.dp, colorScheme.primary, CircleShape)
                        else Modifier
                    )
                    .clickable {
                        pickingIconFor = if (pickingIconFor == "__new_member__") null else "__new_member__"
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    MemberIcons.getIcon(newMemberIcon),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = colorScheme.onSurfaceVariant
                )
            }
            OutlinedTextField(
                value = newMemberName,
                onValueChange = { newMemberName = it },
                placeholder = { Text("New member...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodySmall
            )
            FilledTonalButton(
                onClick = {
                    val trimmed = newMemberName.trim()
                    if (trimmed.isNotEmpty()) {
                        onAddMember(trimmed, newMemberIcon)
                        newMemberName = ""
                        newMemberIcon = "person"
                    }
                },
                enabled = newMemberName.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Add")
            }
        }

        AnimatedVisibility(
            visible = pickingIconFor == "__new_member__",
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            MemberIconPickerRow(
                selectedKey = newMemberIcon,
                onSelect = { key ->
                    newMemberIcon = key
                    pickingIconFor = null
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun MemberAvatarCircle(
    member: FamilyMemberEntity,
    size: Int,
    showCameraBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (member.photoUri.isNotEmpty()) {
            AsyncImage(
                model = File(member.photoUri),
                contentDescription = member.name,
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else if (member.iconKey.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    MemberIcons.getIcon(member.iconKey),
                    contentDescription = member.name,
                    modifier = Modifier.size((size * 0.5f).dp),
                    tint = colorScheme.onPrimaryContainer
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    member.avatar,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        if (showCameraBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(16.dp)
                    .background(colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Change photo",
                    modifier = Modifier.size(9.dp),
                    tint = colorScheme.onPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MemberIconPickerRow(
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    FlowRow(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MemberIcons.allIcons.forEach { (key, icon) ->
            val isSelected = key == selectedKey
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) colorScheme.primary
                        else colorScheme.surfaceContainerHigh
                    )
                    .clickable { onSelect(key) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = key,
                    modifier = Modifier.size(16.dp),
                    tint = if (isSelected) colorScheme.onPrimary
                    else colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
