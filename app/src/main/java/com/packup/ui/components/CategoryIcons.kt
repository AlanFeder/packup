package com.packup.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material.icons.outlined.BabyChangingStation
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.ShieldMoon
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIcons {
    val iconMap: Map<String, ImageVector> = mapOf(
        "shirt" to Icons.Outlined.Checkroom,
        "droplets" to Icons.Outlined.Opacity,
        "zap" to Icons.Outlined.Bolt,
        "file-text" to Icons.Outlined.Description,
        "sun" to Icons.Outlined.WbSunny,
        "watch" to Icons.Outlined.Watch,
        "heart-pulse" to Icons.Outlined.FavoriteBorder,
        "gamepad-2" to Icons.Outlined.SportsEsports,
        "cookie" to Icons.Outlined.Cookie,
        "bed-double" to Icons.Outlined.Bed,
        "wrench" to Icons.Outlined.Build,
        "toy-brick" to Icons.Outlined.BabyChangingStation,
        "shield-check" to Icons.Outlined.ShieldMoon,
        "package" to Icons.Outlined.Inventory2,
        "briefcase" to Icons.Outlined.Work,
        "plane" to Icons.Outlined.FlightTakeoff,
        "camera" to Icons.Outlined.CameraAlt,
        "umbrella" to Icons.Outlined.Umbrella,
        "backpack" to Icons.Outlined.Backpack,
    )

    fun isEmoji(key: String): Boolean = key.isNotEmpty() && !iconMap.containsKey(key)

    fun getIcon(key: String): ImageVector =
        iconMap[key] ?: Icons.Outlined.Inventory2

    val allIcons: List<Pair<String, ImageVector>> = iconMap.toList()
}
