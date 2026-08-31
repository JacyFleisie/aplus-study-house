package com.example.blankapp.screens.parent

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.blankapp.ui.theme.*

// Bottom navigation per spec: Home · Children · Finance · Messages · Profile
enum class ParentTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    CHILDREN("Children", Icons.Filled.ChildCare, Icons.Outlined.ChildCare),
    FINANCE("Finance", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    MESSAGES("Messages", Icons.Filled.Mail, Icons.Outlined.Mail),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    onLogout: () -> Unit,
    onNavigateToRegistration: () -> Unit = {},
    onNavigateToChildProfile: (String) -> Unit = {},
    onNavigateToFinancePayment: (String, Double, String) -> Unit = { _, _, _ -> }
) {
    var selectedTab by remember { mutableStateOf(ParentTab.HOME) }
    var showNotifications by remember { mutableStateOf(false) }
    var showDocuments by remember { mutableStateOf(false) }
    var showCompose by remember { mutableStateOf(false) }

    // Gate: Require at least one registered child
    com.example.blankapp.navigation.RequireRegisteredChild(
        onRegisterChild = onNavigateToRegistration
    ) {

    if (showNotifications) {
        ParentNotificationsScreen(
            onBack = { showNotifications = false }
        )
        return@RequireRegisteredChild
    }

    if (showDocuments) {
        ParentDocumentsScreen(
            onBack = { showDocuments = false }
        )
        return@RequireRegisteredChild
    }

    if (showCompose) {
        ComposeMessageScreen(
            onBack = { showCompose = false },
            onSent = { showCompose = false }
        )
        return@RequireRegisteredChild
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            ParentTab.HOME -> "A+ Study House"
                            ParentTab.CHILDREN -> "My Children"
                            ParentTab.FINANCE -> "Finance"
                            ParentTab.MESSAGES -> "Messages"
                            ParentTab.PROFILE -> "Profile"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    if (selectedTab == ParentTab.MESSAGES) {
                        IconButton(onClick = { showCompose = true }) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Compose",
                                tint = OnBackground
                            )
                        }
                    }
                    IconButton(onClick = { showDocuments = true }) {
                        Icon(
                            Icons.Filled.Folder,
                            contentDescription = "Documents",
                            tint = OnBackground
                        )
                    }
                    IconButton(onClick = { showNotifications = true }) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = OnBackground
                        )
                    }
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
                ParentTab.entries.forEach { tab ->
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
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            unselectedIconColor = OnSurfaceVariant,
                            unselectedTextColor = OnSurfaceVariant,
                            indicatorColor = PrimaryContainer
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
                ParentTab.HOME -> ParentHomeScreen(
                    onNavigateToChildren = { selectedTab = ParentTab.CHILDREN },
                    onNavigateToFinance = { selectedTab = ParentTab.FINANCE },
                    onNavigateToDocuments = { showDocuments = true },
                    onNavigateToRegistration = onNavigateToRegistration
                )
                ParentTab.CHILDREN -> ParentChildrenScreen(
                    onChildClick = onNavigateToChildProfile
                )
                ParentTab.FINANCE -> ParentFinanceScreen(
                    onNavigateToPayment = onNavigateToFinancePayment
                )
                ParentTab.MESSAGES -> ParentMessagesScreen()
                ParentTab.PROFILE -> ParentProfileScreen(
                    onLogout = onLogout,
                    onNavigateToNotifications = { showNotifications = true }
                )
            }
        }
    } // End Scaffold
    } // End RequireRegisteredChild
}
