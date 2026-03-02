package com.packup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.packup.data.local.entity.ItemStatus
import com.packup.ui.components.AddItemForm
import com.packup.ui.components.CategoryGroup
import com.packup.ui.components.CategoryManagerContent
import com.packup.ui.components.DoneSection
import com.packup.ui.components.MemberManagerContent
import com.packup.ui.components.MemberSelector
import com.packup.ui.theme.LocalExtendedColors
import com.packup.viewmodel.PackingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackingScreen(
    viewModel: PackingViewModel = hiltViewModel()
) {
    val membersWithItems by viewModel.membersWithItems.collectAsStateWithLifecycle()
    val activeMemberId by viewModel.activeMemberId.collectAsStateWithLifecycle()
    val isMorningView by viewModel.isMorningView.collectAsStateWithLifecycle()
    val activeMemberWithItems by viewModel.activeMemberWithItems.collectAsStateWithLifecycle()
    val morningItems by viewModel.morningItems.collectAsStateWithLifecycle()
    val snoozedItems by viewModel.snoozedItems.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val totalMorningCount by viewModel.totalMorningCount.collectAsStateWithLifecycle()

    var showResetDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset all items?") },
            text = { Text("This will uncheck all packed items and return snoozed items to their original lists. Your members, categories, and items won't be changed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAll()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSettings) {
        SettingsScreen(
            members = membersWithItems.map { it.member },
            categories = categories,
            onAddMember = viewModel::addMember,
            onRenameMember = viewModel::renameMember,
            onDeleteMember = viewModel::deleteMember,
            onSetMemberIcon = viewModel::setMemberIcon,
            onSetMemberPhoto = viewModel::setMemberPhoto,
            onAddCategory = viewModel::addCategory,
            onRenameCategory = viewModel::renameCategory,
            onDeleteCategory = viewModel::deleteCategory,
            onSetCategoryIcon = viewModel::setCategoryIcon,
            onBack = { showSettings = false }
        )
        return
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                MemberSelector(
                    members = membersWithItems,
                    activeMemberId = activeMemberId,
                    totalMorningCount = totalMorningCount,
                    onSelect = viewModel::selectMember,
                    onReset = { showResetDialog = true },
                    onSettings = { showSettings = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }
    ) { padding ->
        if (isMorningView) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                MorningContent(
                    membersWithItems = membersWithItems,
                    morningItems = morningItems,
                    snoozedItems = snoozedItems,
                    onToggleSnoozedDone = viewModel::toggleSnoozedDone,
                    onUnsnooze = viewModel::unsnoozeItem,
                    onToggleMorningDone = viewModel::toggleMorningItemDone,
                    onEditMorningItem = viewModel::editMorningItem,
                    onDeleteMorningItem = viewModel::deleteMorningItem,
                    onAddMorningItem = viewModel::addMorningItem
                )
            }
        } else {
            val mwi = activeMemberWithItems
            if (mwi != null) {
                val categoryNames = categories.map { it.name }
                val categoryMap = categories.associateBy { it.name }

                val itemsByCategory = mwi.items
                    .filter { it.status != ItemStatus.SNOOZED }
                    .groupBy { it.category }

                val sortedCategories = itemsByCategory.entries
                    .sortedWith(compareBy { entry ->
                        if (entry.value.any { it.status == ItemStatus.TODO }) 0 else 1
                    })

                val doneItems = mwi.items.filter { it.status == ItemStatus.DONE }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        sortedCategories.forEach { (category, items) ->
                            CategoryGroup(
                                category = category,
                                categoryEntity = categoryMap[category],
                                items = items,
                                onToggleDone = viewModel::toggleDone,
                                onSnooze = viewModel::snoozeItem,
                                onEdit = viewModel::editItem,
                                onDelete = viewModel::deleteItem,
                                onMarkAllDone = viewModel::markAllDoneInCategory
                            )
                        }
                    }

                    if (mwi.allDone) {
                        val extendedColors = LocalExtendedColors.current
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(extendedColors.successContainer.copy(alpha = 0.3f))
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = extendedColors.success
                            )
                            Text(
                                "All packed!",
                                style = MaterialTheme.typography.titleMedium,
                                color = extendedColors.success
                            )
                            Text(
                                if (mwi.snoozedCount > 0)
                                    "${mwi.doneCount} packed, ${mwi.snoozedCount} snoozed to morning"
                                else
                                    "Everything is packed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    AddItemForm(
                        categories = categoryNames,
                        onAdd = viewModel::addItem
                    )

                    DoneSection(
                        items = doneItems,
                        onToggleDone = viewModel::toggleDone,
                        onEdit = viewModel::editItem,
                        onDelete = viewModel::deleteItem
                    )

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    members: List<com.packup.data.local.entity.FamilyMemberEntity>,
    categories: List<com.packup.data.local.entity.CategoryEntity>,
    onAddMember: (String, String) -> Unit,
    onRenameMember: (String, String) -> Unit,
    onDeleteMember: (String) -> Unit,
    onSetMemberIcon: (String, String) -> Unit,
    onSetMemberPhoto: (String, String) -> Unit,
    onAddCategory: (String, String) -> Unit,
    onRenameCategory: (String, String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onSetCategoryIcon: (String, String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            MemberManagerContent(
                members = members,
                onAddMember = onAddMember,
                onRenameMember = onRenameMember,
                onDeleteMember = onDeleteMember,
                onSetMemberIcon = onSetMemberIcon,
                onSetMemberPhoto = onSetMemberPhoto,
                onDismiss = onBack
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            CategoryManagerContent(
                categories = categories,
                onAddCategory = onAddCategory,
                onRenameCategory = onRenameCategory,
                onDeleteCategory = onDeleteCategory,
                onSetCategoryIcon = onSetCategoryIcon,
                onDismiss = onBack
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
