package com.packup.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.packup.MainActivity
import com.packup.data.local.entity.ItemStatus
import com.packup.data.local.entity.MorningItemStatus

class MorningWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = WidgetDatabaseProvider.database(context)
        val snoozedItems = db.packingItemDao().getSnoozedItemsSync()
        val morningItems = db.morningItemDao().getAllItemsSync()
        val members = db.familyMemberDao().getAllMembersSync()
        val memberMap = members.associateBy { it.id }

        val rows = mutableListOf<WidgetRowData>()

        val snoozedByMember = snoozedItems.groupBy { it.memberId }
        for ((memberId, items) in snoozedByMember) {
            val pending = items.filter { it.status != ItemStatus.DONE }
            if (pending.isEmpty()) continue
            val memberName = memberMap[memberId]?.name ?: "Unknown"
            rows.add(WidgetRowData(id = "header_$memberId", label = memberName, isSnoozed = false, sectionHeader = memberName))
            for (item in pending) {
                rows.add(WidgetRowData(id = item.id, label = item.name, isSnoozed = true))
            }
        }

        val pendingMorning = morningItems.filter { it.status != MorningItemStatus.DONE }
        if (pendingMorning.isNotEmpty()) {
            rows.add(WidgetRowData(id = "header_morning", label = "Before Leaving", isSnoozed = false, sectionHeader = "Before Leaving"))
            for (item in pendingMorning) {
                rows.add(WidgetRowData(id = item.id, label = item.name, isSnoozed = false))
            }
        }

        val remaining = rows.count { it.sectionHeader == null }

        provideContent {
            GlanceTheme {
                MorningWidgetContent(rows, remaining)
            }
        }
    }

    @Composable
    private fun MorningWidgetContent(
        rows: List<WidgetRowData>,
        remaining: Int,
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .padding(12.dp),
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "AM Packing List",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = if (remaining > 0) "$remaining left" else "All done!",
                    style = TextStyle(
                        color = GlanceTheme.colors.secondary,
                        fontSize = 13.sp,
                    ),
                )
            }

            if (rows.isEmpty()) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "No morning items yet",
                        style = TextStyle(
                            color = GlanceTheme.colors.secondary,
                            fontSize = 13.sp,
                        ),
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(rows, itemId = { it.id.hashCode().toLong() }) { row ->
                        if (row.sectionHeader != null) {
                            SectionHeader(row.sectionHeader)
                        } else {
                            ChecklistItem(row)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SectionHeader(title: String) {
        Text(
            text = title,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            ),
            modifier = GlanceModifier.padding(top = 8.dp, bottom = 4.dp),
        )
    }

    @Composable
    private fun ChecklistItem(row: WidgetRowData) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(
                    actionRunCallback<ToggleItemAction>(
                        actionParametersOf(
                            ToggleItemAction.itemIdKey to row.id,
                            ToggleItemAction.isSnoozedKey to row.isSnoozed,
                        )
                    )
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "☐",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 18.sp,
                ),
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = row.label,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                ),
                modifier = GlanceModifier.defaultWeight(),
            )
        }
    }
}

internal data class WidgetRowData(
    val id: String,
    val label: String,
    val isSnoozed: Boolean,
    val sectionHeader: String? = null,
)

class ToggleItemAction : ActionCallback {

    companion object {
        val itemIdKey = ActionParameters.Key<String>("item_id")
        val isSnoozedKey = ActionParameters.Key<Boolean>("is_snoozed")
    }

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val itemId = parameters[itemIdKey] ?: return
        val isSnoozed = parameters[isSnoozedKey] ?: return

        val db = WidgetDatabaseProvider.database(context)

        if (isSnoozed) {
            db.packingItemDao().updateStatus(itemId, ItemStatus.DONE)
        } else {
            db.morningItemDao().updateStatus(itemId, MorningItemStatus.DONE)
        }

        MorningWidget().update(context, glanceId)
    }
}
