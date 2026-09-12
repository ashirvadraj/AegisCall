package com.aegiscall.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aegiscall.app.data.AegisDatabase
import com.aegiscall.app.data.entity.CallLogEntity
import com.aegiscall.app.data.entity.CallType
import com.aegiscall.app.ui.theme.AegisDangerRed
import com.aegiscall.app.ui.theme.AegisShieldGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallHistoryScreen() {
    val context = LocalContext.current
    val database = remember { AegisDatabase.getDatabase(context) }
    val callLogs by database.callLogDao().getAllCallLogs().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Call History", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            if (callLogs.isNotEmpty()) {
                IconButton(onClick = {
                    coroutineScope.launch { database.callLogDao().clearAll() }
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear All")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (callLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No call logs yet.\nCalls will be automatically logged and screened.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(callLogs) { log ->
                    CallLogItem(
                        log = log,
                        onWhatsAppClick = { number ->
                            val clean = number.replace("[^0-9]".toRegex(), "")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$clean")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try { context.startActivity(intent) } catch (e: Exception) {}
                        },
                        onBlockClick = { number ->
                            coroutineScope.launch {
                                database.blockedNumberDao().insertBlockedNumber(
                                    com.aegiscall.app.data.entity.BlockedNumberEntity(
                                        phoneNumberOrPattern = number,
                                        reason = "Manual user block"
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CallLogItem(
    log: CallLogEntity,
    onWhatsAppClick: (String) -> Unit,
    onBlockClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = log.contactName ?: log.phoneNumber,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${log.phoneNumber} ? ${formatTimestamp(log.timestamp)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val (typeText, badgeColor) = when (log.callType) {
                    CallType.INCOMING -> "Incoming" to AegisShieldGreen
                    CallType.OUTGOING -> "Outgoing" to Color(0xFF2196F3)
                    CallType.MISSED -> "Missed" to AegisDangerRed
                    CallType.BLOCKED_SPAM -> "??? Blocked" to AegisDangerRed
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = typeText,
                        color = badgeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onWhatsAppClick(log.phoneNumber) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { onBlockClick(log.phoneNumber) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AegisDangerRed)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = "Block", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Block", fontSize = 12.sp)
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
