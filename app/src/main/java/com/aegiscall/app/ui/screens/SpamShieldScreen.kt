package com.aegiscall.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aegiscall.app.data.AegisDatabase
import com.aegiscall.app.data.entity.BlockedNumberEntity
import com.aegiscall.app.ui.theme.AegisDangerRed
import com.aegiscall.app.ui.theme.AegisShieldGreen
import kotlinx.coroutines.launch

@Composable
fun SpamShieldScreen() {
    val context = LocalContext.current
    val database = remember { AegisDatabase.getDatabase(context) }
    val blockedList by database.blockedNumberDao().getAllBlockedNumbers().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    var autoBlockEnabled by remember { mutableStateOf(true) }
    var blockForeignEnabled by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newBlockNumber by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = "Shield", tint = AegisShieldGreen, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Aegis Shield Protection", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Auto-Drop Top Spammers", fontWeight = FontWeight.SemiBold)
                        Text("Silently blocks calls from known scam networks", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = autoBlockEnabled, onCheckedChange = { autoBlockEnabled = it })
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Block Foreign Area Codes", fontWeight = FontWeight.SemiBold)
                        Text("Screen international robocalls", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = blockForeignEnabled, onCheckedChange = { blockForeignEnabled = it })
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Custom Blacklist (${blockedList.size})", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(blockedList) { blocked ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(blocked.phoneNumberOrPattern, fontWeight = FontWeight.Bold)
                            Text(blocked.reason, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = {
                            coroutineScope.launch { database.blockedNumberDao().deleteByPattern(blocked.phoneNumberOrPattern) }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Unblock", tint = AegisDangerRed)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Block Number or Wildcard") },
            text = {
                Column {
                    Text("Enter phone number or pattern (e.g. +1800%):", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newBlockNumber,
                        onValueChange = { newBlockNumber = it },
                        label = { Text("Phone Number / Pattern") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newBlockNumber.isNotEmpty()) {
                        coroutineScope.launch {
                            database.blockedNumberDao().insertBlockedNumber(
                                BlockedNumberEntity(phoneNumberOrPattern = newBlockNumber, reason = "User Block")
                            )
                            newBlockNumber = ""
                            showAddDialog = false
                        }
                    }
                }) { Text("Add Rule") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
