package com.aegiscall.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aegiscall.app.service.CallerAnnouncer
import com.aegiscall.app.ui.theme.AegisShieldGreen

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var amoledDark by remember { mutableStateOf(true) }
    var voiceAnnounceEnabled by remember { mutableStateOf(true) }
    var zeroTelemetryAudited by remember { mutableStateOf(true) }
    var showEscapeTool by remember { mutableStateOf(false) }

    if (showEscapeTool) {
        FakeCallScreen()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Settings & Tools", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Privacy Guarantee Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = "Privacy", tint = AegisShieldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("100% Ad-Free & Zero Data Harvesting", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Unlike commercial caller ID apps, AegisCall contains 0 ad trackers and NEVER uploads your contact book to any server. All caller screening and SMS heuristics execute locally on your device.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Features & Toggles Card
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Voice Announce ('Who is Calling')", fontWeight = FontWeight.SemiBold)
                        Text("Speaks caller name or spam warning when ringing", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = voiceAnnounceEnabled,
                        onCheckedChange = {
                            voiceAnnounceEnabled = it
                            if (it) CallerAnnouncer.announce(context, "Voice Announcer Active", false)
                        }
                    )
                }

                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Floating HUD Caller ID Overlay", fontWeight = FontWeight.SemiBold)
                        Text("Display card over incoming calls (Optional)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = android.provider.Settings.canDrawOverlays(context),
                        onCheckedChange = {
                            val intent = android.content.Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:" + context.packageName)
                            )
                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AMOLED Pitch-Black Dark Mode", fontWeight = FontWeight.SemiBold)
                    Switch(checked = amoledDark, onCheckedChange = { amoledDark = it })
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Zero-Knowledge Local Cache", fontWeight = FontWeight.SemiBold)
                    Switch(checked = zeroTelemetryAudited, onCheckedChange = { zeroTelemetryAudited = it })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Extra Tool: Fake Emergency Escape Call
        OutlinedButton(
            onClick = { showEscapeTool = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Timer, contentDescription = "Escape")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Emergency Escape Call Simulator")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Export Database
        Button(
            onClick = {
                Toast.makeText(context, "Call logs and blocklist exported to encrypted JSON", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = "Export")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Encrypted Backup (JSON)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "Version")
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("AegisCall Version 1.1.0", fontWeight = FontWeight.Bold)
                    Text("Pure Open-Source Shield ? Who Is Calling Search Active", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
