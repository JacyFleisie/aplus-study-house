package com.example.blankapp.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.blankapp.screens.admin.tabs.*
import com.example.blankapp.ui.theme.*

enum class AdminTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    APPLICATIONS("Applications", Icons.Filled.Assignment, Icons.Outlined.Assignment),
    STUDENTS("Students", Icons.Filled.School, Icons.Outlined.School),
    MESSAGES("Messages", Icons.Filled.Mail, Icons.Outlined.Mail),
    FINANCE("Finance", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit,
    onNavigateToApplication: (String) -> Unit = {},
    onNavigateToStudent: (String) -> Unit = {},
    onNavigateToFinance: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(AdminTab.HOME) }
    var showCrashLogs by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    if (showCrashLogs) {
        CrashLogScreen(onBackClick = { showCrashLogs = false })
        return
    }

    // About screen has its own top bar - hide bottom nav
    if (showAbout) {
        AboutScreen(onBack = { showAbout = false })
        return
    }

    // Messages tab has its own top bar - hide bottom nav
    if (selectedTab == AdminTab.MESSAGES) {
        AdminMessagesScreen(onBack = { selectedTab = AdminTab.HOME })
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            AdminTab.HOME -> "Admin Dashboard"
                            AdminTab.APPLICATIONS -> "Applications"
                            AdminTab.STUDENTS -> "Students"
                            AdminTab.MESSAGES -> "Messages"
                            AdminTab.FINANCE -> "Finance"
                            AdminTab.SETTINGS -> "Settings"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // No inbox icon in top bar - messages are now a tab
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = OnBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                AdminTab.entries.forEach { tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (selectedTab == tab) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) },
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Secondary,
                            selectedTextColor = Secondary,
                            unselectedIconColor = OnSurfaceVariant,
                            unselectedTextColor = OnSurfaceVariant,
                            indicatorColor = SecondaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                AdminTab.HOME ->                AdminHomeTab(
                    onNavigateToApplication = onNavigateToApplication
                )
                AdminTab.APPLICATIONS -> AdminApplicationsTab(onNavigateToApplication = onNavigateToApplication)
                AdminTab.STUDENTS -> AdminStudentsTab(onNavigateToStudent = onNavigateToStudent)
                AdminTab.MESSAGES -> AdminMessagesScreen(onBack = { selectedTab = AdminTab.HOME })
                AdminTab.FINANCE -> AdminFinanceTab(onNavigateToFinance = onNavigateToFinance)
                AdminTab.SETTINGS -> AdminSettingsTab(
                    onLogout = onLogout,
                    onNavigateToCrashLogs = { showCrashLogs = true },
                    onNavigateToAbout = { showAbout = true }
                )
            }
        }
    }
}
