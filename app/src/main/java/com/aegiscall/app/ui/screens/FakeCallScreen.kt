package com.aegiscall.app.ui.screens

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aegiscall.app.ui.theme.AegisDangerRed
import com.aegiscall.app.ui.theme.AegisShieldGreen

@Composable
fun FakeCallScreen() {
    val context = LocalContext.current
    var callerName by remember { mutableStateOf("Boss / Emergency") }
    var callerNumber by remember { mutableStateOf("+1 555-0199") }
    var delaySeconds by remember { mutableStateOf(10) }
    var isSimulatingIncoming by remember { mutableStateOf(false) }

    if (isSimulatingIncoming) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 48.dp)
                ) {
                    Text("Incoming Call...", color = Color.LightGray, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(callerName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(callerNumber, color = Color.Gray, fontSize = 18.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(
                        onClick = { isSimulatingIncoming = false },
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(AegisDangerRed)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "Decline", tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Call Connected (Simulation)", Toast.LENGTH_SHORT).show()
                            isSimulatingIncoming = false
                        },
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(AegisShieldGreen)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Answer", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Emergency Escape Fake Call", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "Simulate a real incoming call with custom caller identity to escape uncomfortable situations.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = callerName,
                        onValueChange = { callerName = it },
                        label = { Text("Caller Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = callerNumber,
                        onValueChange = { callerNumber = it },
                        label = { Text("Caller Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Schedule Delay: $delaySeconds seconds", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = delaySeconds.toFloat(),
                        onValueChange = { delaySeconds = it.toInt() },
                        valueRange = 5f..60f,
                        steps = 11
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Escape call in $delaySeconds seconds!", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        isSimulatingIncoming = true
                    }, (delaySeconds * 1000).toLong())
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AegisShieldGreen)
            ) {
                Icon(Icons.Default.Timer, contentDescription = "Timer")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Schedule Escape Call ($delaySeconds s)", fontWeight = FontWeight.Bold)
            }
        }
    }
}
