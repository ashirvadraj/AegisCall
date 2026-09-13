package com.aegiscall.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.aegiscall.app.data.AegisDatabase
import com.aegiscall.app.data.entity.BlockedNumberEntity
import com.aegiscall.app.data.entity.CachedCallerEntity
import com.aegiscall.app.data.entity.SpamRiskLevel
import com.aegiscall.app.engine.NumberLookupEngine
import com.aegiscall.app.engine.SearchResultItem
import com.aegiscall.app.engine.UpiLookupHelper
import com.aegiscall.app.service.CallerAnnouncer
import com.aegiscall.app.ui.theme.AegisBlue
import com.aegiscall.app.ui.theme.AegisDangerRed
import com.aegiscall.app.ui.theme.AegisShieldGreen
import com.aegiscall.app.ui.theme.AegisWarningOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LookupScreen() {
    val context = LocalContext.current
    val database = remember { AegisDatabase.getDatabase(context) }
    val lookupEngine = remember { NumberLookupEngine(context) }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<SearchResultItem>>(emptyList()) }

    var editingNumber by remember { mutableStateOf<String?>(null) }
    var customNameInput by remember { mutableStateOf("") }

    val recentUnknowns by database.callLogDao().getRecentUnknownCalls().collectAsState(initial = emptyList())
    val topSpammers by database.spamSignatureDao().getTopSpammers().collectAsState(initial = emptyList())

    fun performSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            searchResults = emptyList()
            return
        }
        isSearching = true
        coroutineScope.launch {
            val results = withContext(Dispatchers.IO) {
                lookupEngine.searchNumberOrName(trimmed)
            }
            searchResults = results
            isSearching = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, contentDescription = "Lookup", tint = AegisBlue, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Who Is Calling? / Truecaller Identity", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("Exact name, telecom operator & bank identity resolution", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                performSearch(it)
            },
            label = { Text("Search any number (e.g. 7808594583) or name") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        searchResults = emptyList()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Search Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                SuggestionChip(
                    onClick = {
                        searchQuery = "7808594583"
                        performSearch("7808594583")
                    },
                    label = { Text("? Test 7808594583") }
                )
            }

            item {
                SuggestionChip(
                    onClick = {
                        val num = if (searchQuery.isNotBlank()) searchQuery else "7808594583"
                        UpiLookupHelper.openTruecallerWeb(context, num)
                    },
                    label = { Text("?? Search Truecaller Web") }
                )
            }

            item {
                SuggestionChip(
                    onClick = {
                        val num = if (searchQuery.isNotBlank()) searchQuery else "7808594583"
                        UpiLookupHelper.verifyBankName(context, num)
                    },
                    label = { Text("?? Verify Bank Name (UPI)") }
                )
            }

            item {
                SuggestionChip(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                        if (!clip.isNullOrBlank()) {
                            searchQuery = clip
                            performSearch(clip)
                            Toast.makeText(context, "Pasted: $clip", Toast.LENGTH_SHORT).show()
                        }
                    },
                    label = { Text("?? Paste Clipboard") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isSearching) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AegisBlue)
            }
        } else if (searchQuery.isNotEmpty()) {
            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonSearch, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Searching number '$searchQuery'...", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { UpiLookupHelper.openTruecallerWeb(context, searchQuery) }) {
                            Text("Open Truecaller Search for $searchQuery")
                        }
                    }
                }
            } else {
                Text("Identified Caller Result (${searchResults.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(searchResults) { item ->
                        SearchResultCard(
                            item = item,
                            onCall = { num ->
                                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$num")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try { context.startActivity(intent) } catch (e: Exception) {}
                            },
                            onWhatsApp = { num ->
                                val clean = if (num.startsWith("+")) num.substring(1) else if (num.length == 10) "91$num" else num
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$clean")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try { context.startActivity(intent) } catch (e: Exception) {}
                            },
                            onTruecallerWeb = { num ->
                                UpiLookupHelper.openTruecallerWeb(context, num)
                            },
                            onUpiBankVerify = { num ->
                                UpiLookupHelper.verifyBankName(context, num)
                            },
                            onBlock = { num ->
                                coroutineScope.launch {
                                    database.blockedNumberDao().insertBlockedNumber(
                                        BlockedNumberEntity(phoneNumberOrPattern = num, reason = "Blocked from Search")
                                    )
                                    Toast.makeText(context, "Blocked $num", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onAnnounce = { name, isSpam ->
                                CallerAnnouncer.announce(context, name, isSpam)
                            },
                            onEditName = { num, currentName ->
                                editingNumber = num
                                customNameInput = currentName
                            }
                        )
                    }
                }
            }
        } else {
            // Default view: Recent Unknown Callers & Directory
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text("Recent Unknown Numbers Who Called You", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tap to run instant reverse caller lookup", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (recentUnknowns.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text("No recent unknown callers in your history.", modifier = Modifier.padding(16.dp), fontSize = 13.sp)
                        }
                    }
                } else {
                    items(recentUnknowns.take(5)) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                searchQuery = log.phoneNumber
                                performSearch(log.phoneNumber)
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(AegisBlue.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = AegisBlue)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(log.phoneNumber, fontWeight = FontWeight.Bold)
                                        Text("Tap to reveal identity", fontSize = 11.sp, color = AegisBlue)
                                    }
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = "Inspect", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Identified Spam Threats Directory", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                items(topSpammers.take(5)) { spam ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            searchQuery = spam.patternOrNumber
                            performSearch(spam.patternOrNumber)
                        },
                        colors = CardDefaults.cardColors(containerColor = AegisDangerRed.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(spam.reportedName, fontWeight = FontWeight.Bold, color = AegisDangerRed)
                                Text("${spam.patternOrNumber} ? Reported ${spam.totalReports} times", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                color = AegisDangerRed,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("SPAM", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog to save custom caller name
    if (editingNumber != null) {
        AlertDialog(
            onDismissRequest = { editingNumber = null },
            title = { Text("Label / Save Verified Name") },
            text = {
                Column {
                    Text("Assign a permanent verified name to $editingNumber:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customNameInput,
                        onValueChange = { customNameInput = it },
                        label = { Text("Exact Person / Business Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val num = editingNumber ?: return@Button
                    val newName = customNameInput.trim()
                    if (newName.isNotEmpty()) {
                        coroutineScope.launch {
                            database.cachedCallerDao().insertCachedCaller(
                                CachedCallerEntity(
                                    phoneNumber = num,
                                    resolvedName = newName,
                                    carrier = "Verified Contact",
                                    circleOrCity = "Saved in AegisCall",
                                    isVerified = true
                                )
                            )
                            performSearch(num)
                            editingNumber = null
                            Toast.makeText(context, "Saved name: $newName", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text("Save Name") }
            },
            dismissButton = {
                TextButton(onClick = { editingNumber = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SearchResultCard(
    item: SearchResultItem,
    onCall: (String) -> Unit,
    onWhatsApp: (String) -> Unit,
    onTruecallerWeb: (String) -> Unit,
    onUpiBankVerify: (String) -> Unit,
    onBlock: (String) -> Unit,
    onAnnounce: (String, Boolean) -> Unit,
    onEditName: (String, String) -> Unit
) {
    val isSpam = item.riskLevel == SpamRiskLevel.HIGH_RISK_SPAM || item.riskLevel == SpamRiskLevel.FRAUD_SCAM
    val badgeColor = when (item.riskLevel) {
        SpamRiskLevel.SAFE -> AegisShieldGreen
        SpamRiskLevel.SUSPICIOUS -> AegisWarningOrange
        SpamRiskLevel.HIGH_RISK_SPAM, SpamRiskLevel.FRAUD_SCAM -> AegisDangerRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSpam) AegisDangerRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with Avatar and Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(badgeColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.displayName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = badgeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.displayName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Name",
                                modifier = Modifier.size(16.dp).clickable { onEditName(item.phoneNumber, item.displayName) },
                                tint = AegisBlue
                            )
                        }
                        Text(item.phoneNumber, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isSpam) "?? SPAM" else "? VERIFIED",
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Telecom & Carrier Identification info
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("?? Operator: ${item.cityOrCarrier}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("?? Source: ${item.matchSource}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (item.callCount > 0) {
                        Text("?? History: Called you ${item.callCount} times", fontSize = 11.sp, color = AegisBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 1: Truecaller Web Search + Bank UPI Legal Name Verification
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onTruecallerWeb(item.phoneNumber) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Language, contentDescription = "Truecaller Web", modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Truecaller Web", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = { onUpiBankVerify(item.phoneNumber) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = "Bank Verify", modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verify Bank Name", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: WhatsApp + Direct Call + Announce + Block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onWhatsApp(item.phoneNumber) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp DP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = { onCall(item.phoneNumber) },
                    colors = ButtonDefaults.buttonColors(containerColor = AegisShieldGreen),
                    modifier = Modifier.weight(0.7f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                IconButton(
                    onClick = { onAnnounce(item.displayName, isSpam) },
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(AegisBlue.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Speak", tint = AegisBlue, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = { onBlock(item.phoneNumber) },
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(AegisDangerRed.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.Shield, contentDescription = "Block", tint = AegisDangerRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
