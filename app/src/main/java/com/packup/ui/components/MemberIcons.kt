package com.packup.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.EmojiPeople
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Face2
import androidx.compose.material.icons.outlined.Face3
import androidx.compose.material.icons.outlined.Face4
import androidx.compose.material.icons.outlined.Face5
import androidx.compose.material.icons.outlined.Face6
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Girl
import androidx.compose.material.icons.outlined.Boy
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.ui.graphics.vector.ImageVector

object MemberIcons {
    val iconMap: Map<String, ImageVector> = mapOf(
        "person" to Icons.Outlined.Person,
        "person-outline" to Icons.Outlined.PersonOutline,
        "face" to Icons.Outlined.Face,
        "face2" to Icons.Outlined.Face2,
        "face3" to Icons.Outlined.Face3,
        "face4" to Icons.Outlined.Face4,
        "face5" to Icons.Outlined.Face5,
        "face6" to Icons.Outlined.Face6,
        "child" to Icons.Outlined.ChildCare,
        "girl" to Icons.Outlined.Girl,
        "boy" to Icons.Outlined.Boy,
        "family" to Icons.Outlined.FamilyRestroom,
        "group" to Icons.Outlined.Groups,
        "school" to Icons.Outlined.School,
        "emoji" to Icons.Outlined.EmojiPeople,
        "star" to Icons.Outlined.Star,
        "favorite" to Icons.Outlined.Favorite,
        "pets" to Icons.Outlined.Pets,
        "sports" to Icons.Outlined.SportsBasketball,
    )

    fun getIcon(key: String): ImageVector =
        iconMap[key] ?: Icons.Outlined.Person

    val allIcons: List<Pair<String, ImageVector>> = iconMap.toList()
}
