package com.example.blankapp.screens.parent

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.blankapp.data.PermissionCategory

// Helper functions for permission categories
// Used by both parent and admin screens

fun getPermissionCategoryColor(category: PermissionCategory): Color {
    return when (category) {
        PermissionCategory.EXCURSION -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        PermissionCategory.MEDICAL -> androidx.compose.ui.graphics.Color(0xFFF44336)
        PermissionCategory.PHOTO -> androidx.compose.ui.graphics.Color(0xFF2196F3)
        PermissionCategory.SPORTS -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        PermissionCategory.GENERAL -> androidx.compose.ui.graphics.Color(0xFF607D8B)
        PermissionCategory.OTHER -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
    }
}

fun getPermissionCategoryLabel(category: PermissionCategory): String {
    return when (category) {
        PermissionCategory.EXCURSION -> "Excursion"
        PermissionCategory.MEDICAL -> "Medical"
        PermissionCategory.PHOTO -> "Photo/Video"
        PermissionCategory.SPORTS -> "Sports"
        PermissionCategory.GENERAL -> "General"
        PermissionCategory.OTHER -> "Other"
    }
}

fun getPermissionCategoryIcon(category: PermissionCategory): ImageVector {
    return when (category) {
        PermissionCategory.EXCURSION -> Icons.Filled.DirectionsBus
        PermissionCategory.MEDICAL -> Icons.Filled.MedicalServices
        PermissionCategory.PHOTO -> Icons.Filled.CameraAlt
        PermissionCategory.SPORTS -> Icons.Filled.Sports
        PermissionCategory.GENERAL -> Icons.Filled.Info
        PermissionCategory.OTHER -> Icons.Filled.Folder
    }
}
