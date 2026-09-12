package com.aegiscall.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

enum class NavItem(val title: String) {
    DIALER("Dialer"),
    CALLS("Calls"),
    SMS("SMS"),
    SHIELD("Shield"),
    ESCAPE("Escape"),
    SETTINGS("Settings")
}

@Composable
fun MainNavigation() {
    var currentItem by remember { mutableStateOf(NavItem.DIALER) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dialpad, contentDescription = "Dialer") },
                    label = { Text("Dialer") },
                    selected = currentItem == NavItem.DIALER,
                    onClick = { currentItem = NavItem.DIALER }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
                    label = { Text("Calls") },
                    selected = currentItem == NavItem.CALLS,
                    onClick = { currentItem = NavItem.CALLS }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Message, contentDescription = "SMS") },
                    label = { Text("SMS") },
                    selected = currentItem == NavItem.SMS,
                    onClick = { currentItem = NavItem.SMS }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Security, contentDescription = "Shield") },
                    label = { Text("Shield") },
                    selected = currentItem == NavItem.SHIELD,
                    onClick = { currentItem = NavItem.SHIELD }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Timer, contentDescription = "Escape") },
                    label = { Text("Escape") },
                    selected = currentItem == NavItem.ESCAPE,
                    onClick = { currentItem = NavItem.ESCAPE }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentItem == NavItem.SETTINGS,
                    onClick = { currentItem = NavItem.SETTINGS }
                )
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (currentItem) {
                NavItem.DIALER -> DialerScreen()
                NavItem.CALLS -> CallHistoryScreen()
                NavItem.SMS -> SmsInboxScreen()
                NavItem.SHIELD -> SpamShieldScreen()
                NavItem.ESCAPE -> FakeCallScreen()
                NavItem.SETTINGS -> SettingsScreen()
            }
        }
    }
}
