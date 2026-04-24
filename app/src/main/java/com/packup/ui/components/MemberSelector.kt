package com.packup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.packup.data.local.entity.FamilyMemberEntity
import com.packup.ui.theme.LocalExtendedColors
import com.packup.viewmodel.MemberWithItems

@Composable
fun MemberSelector(
    members: List<MemberWithItems>,
    activeMemberId: String,
    totalMorningCount: Int,
    onSelect: (String) -> Unit,
    onReset: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isMorning = activeMemberId == "morning"

    val active = members.filter { !it.allDone }
    val done = members.filter { it.allDone }

    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(active, key = { it.member.id }) { mwi ->
            MemberTab(
                member = mwi.member,
                isActive = mwi.member.id == activeMemberId,
                allDone = false,
                remaining = mwi.remaining,
                onClick = { onSelect(mwi.member.id) }
            )
        }

        item(key = "morning") {
            MorningTab(
                isActive = isMorning,
                count = totalMorningCount,
                onClick = { onSelect("morning") }
            )
        }

        items(done, key = { it.member.id }) { mwi ->
            MemberTab(
                member = mwi.member,
                isActive = mwi.member.id == activeMemberId,
                allDone = true,
                remaining = 0,
                onClick = { onSelect(mwi.member.id) }
            )
        }

        if (onReset != null || onSettings != null) {
            item(key = "actions") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VerticalDivider(
                        modifier = Modifier
                            .height(40.dp)
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    if (onReset != null) {
                        IconButton(onClick = onReset) {
                            Icon(
                                Icons.Outlined.RestartAlt,
                                contentDescription = "Reset all",
                                modifier = Modifier.size(22.dp),
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (onSettings != null) {
                        IconButton(onClick = onSettings) {
                            Icon(
                                Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(22.dp),
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberTab(
    member: FamilyMemberEntity,
    isActive: Boolean,
    allDone: Boolean,
    remaining: Int,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = LocalExtendedColors.current
    val primaryColor = colorScheme.primary
    val successColor = extendedColors.success
    val hasPhoto = member.photoUri.isNotEmpty()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 64.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(
                if (isActive) colorScheme.primaryContainer.copy(alpha = 0.4f)
                else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (hasPhoto) {
                AsyncImage(
                    model = member.photoUri,
                    contentDescription = member.name,
                    modifier = Modifier
                        .size(48.dp)
                        .then(
                            if (isActive) Modifier.border(2.dp, primaryColor, CircleShape)
                            else if (allDone) Modifier.border(2.dp, successColor.copy(alpha = 0.4f), CircleShape)
                            else Modifier
                        )
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .then(
                            if (isActive) Modifier.border(2.dp, primaryColor, CircleShape)
                            else if (allDone) Modifier.border(2.dp, successColor.copy(alpha = 0.4f), CircleShape)
                            else Modifier
                        )
                        .clip(CircleShape)
                        .background(
                            if (allDone) extendedColors.successContainer.copy(alpha = 0.3f)
                            else colorScheme.surfaceContainerHigh
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (member.iconKey.isNotEmpty()) {
                        Icon(
                            MemberIcons.getIcon(member.iconKey),
                            contentDescription = member.name,
                            modifier = Modifier.size(22.dp),
                            tint = if (allDone) extendedColors.success
                            else if (isActive) colorScheme.primary
                            else colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = member.avatar,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (allDone) extendedColors.success
                            else if (isActive) colorScheme.primary
                            else colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (allDone) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .size(20.dp)
                        .background(extendedColors.success, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = extendedColors.onSuccess
                    )
                }
            } else if (remaining > 0) {
                Badge(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                ) {
                    Text(remaining.toString(), style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Text(
            text = member.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) colorScheme.primary else colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp).widthIn(max = 56.dp)
        )
    }
}

@Composable
private fun MorningTab(
    isActive: Boolean,
    count: Int,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = LocalExtendedColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 64.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(
                if (isActive) extendedColors.warningContainer.copy(alpha = 0.4f)
                else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .then(
                        if (isActive) Modifier.border(2.dp, extendedColors.warning, CircleShape)
                        else Modifier
                    )
                    .clip(CircleShape)
                    .background(
                        if (isActive) extendedColors.warningContainer.copy(alpha = 0.5f)
                        else extendedColors.warningContainer.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.WbTwilight,
                    contentDescription = "Morning",
                    modifier = Modifier.size(22.dp),
                    tint = if (isActive) extendedColors.onWarningContainer
                    else extendedColors.warning.copy(alpha = 0.7f)
                )
            }

            if (count > 0) {
                Badge(
                    containerColor = extendedColors.warning,
                    contentColor = extendedColors.onWarning,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                ) {
                    Text(count.toString(), style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Text(
            text = "Morning",
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) extendedColors.onWarningContainer else colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
