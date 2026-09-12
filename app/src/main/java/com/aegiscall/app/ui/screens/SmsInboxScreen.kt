package com.aegiscall.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aegiscall.app.data.AegisDatabase
import com.aegiscall.app.data.entity.SmsCategory
import com.aegiscall.app.data.entity.SmsMessageEntity
import com.aegiscall.app.ui.theme.AegisDangerRed
import com.aegiscall.app.ui.theme.AegisShieldGreen

@Composable
fun SmsInboxScreen() {
    val context = LocalContext.current
    val database = remember { AegisDatabase.getDatabase(context) }
    var selectedCategory by remember { mutableStateOf<SmsCategory?>(null) }

    val messages by if (selectedCategory == null) {
        database.smsDao().getAllMessages().collectAsState(initial = emptyList())
    } else {
        database.smsDao().getMessagesByCategory(selectedCategory!!).collectAsState(initial = emptyList())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Smart SMS Organizer", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        ScrollableTabRow(
            selectedTabIndex = when (selectedCategory) {
                null -> 0
                SmsCategory.PERSONAL -> 1
                SmsCategory.OTP_2FA -> 2
                SmsCategory.TRANSACTIONAL -> 3
                SmsCategory.SPAM_QUARANTINE -> 4
                SmsCategory.PROMOTION -> 5
            },
            edgePadding = 0.dp,
            divider = {}
        ) {
            Tab(selected = selectedCategory == null, onClick = { selectedCategory = null }, text = { Text("All") })
            Tab(selected = selectedCategory == SmsCategory.PERSONAL, onClick = { selectedCategory = SmsCategory.PERSONAL }, text = { Text("Personal") })
            Tab(selected = selectedCategory == SmsCategory.OTP_2FA, onClick = { selectedCategory = SmsCategory.OTP_2FA }, text = { Text("OTPs") })
            Tab(selected = selectedCategory == SmsCategory.TRANSACTIONAL, onClick = { selectedCategory = SmsCategory.TRANSACTIONAL }, text = { Text("Bank") })
            Tab(selected = selectedCategory == SmsCategory.SPAM_QUARANTINE, onClick = { selectedCategory = SmsCategory.SPAM_QUARANTINE }, text = { Text("??? Spam") })
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (messages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No messages found.\nIncoming SMS will be categorized here automatically.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(messages) { msg ->
                    SmsCard(msg = msg)
                }
            }
        }
    }
}

@Composable
fun SmsCard(msg: SmsMessageEntity) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (msg.category == SmsCategory.SPAM_QUARANTINE) AegisDangerRed.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(msg.sender, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Surface(
                    color = if (msg.category == SmsCategory.SPAM_QUARANTINE) AegisDangerRed else AegisShieldGreen,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = msg.category.name,
                        color = MaterialTheme.colorScheme.surface,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(msg.body, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)

            if (msg.extractedOtp != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("OTP", msg.extractedOtp))
                        Toast.makeText(context, "OTP ${msg.extractedOtp} copied!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AegisShieldGreen)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Code: ${msg.extractedOtp}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
