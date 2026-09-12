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
import com.aegiscall.app.data.entity.SpamRiskLevel
import com.aegiscall.app.engine.NumberLookupEngine
import com.aegiscall.app.engine.SearchResultItem
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

    // Recent unknown calls from database
    val recentUnknowns by database.callLogDao().getRecentUnknownCalls().collectAsState(initial = emptyList())
    val topSpammers by database.spamSignatureDao().getTopSpammers().collectAsState(initial = emptyList())

    fun performSearch(query: String) {
        if (query.isBlank()) {
            searchResults = emptyList()
            return
        }
        isSearching = true
        coroutineScope.launch {
            val results = withContext(Dispatchers.IO) {
                lookupEngine.searchNumberOrName(query)
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
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = "Lookup", tint = AegisBlue, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Who Is Calling? / Number Lookup", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Search by number or name to reveal caller identity", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                performSearch(it)
            },
            label = { Text("Enter number or name (e.g. +1800..., John, Amazon)") },
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

        // Quick Search Action Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                SuggestionChip(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                        if (!clip.isNullOrBlank()) {
                            searchQuery = clip
                            performSearch(clip)
                            Toast.makeText(context, "Pasted from clipboard: $clip", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    label = { Text("?? Paste Clipboard") }
                )
            }

            item {
                SuggestionChip(
                    onClick = {
                        CallerAnnouncer.announce(context, "Alex from Logistics", false)
                        Toast.makeText(context, "Voice Announcer Playing...", Toast.LENGTH_SHORT).show()
                    },
                    label = { Text("?? Test Voice Announce") }
                )
            }

            item {
                SuggestionChip(
                    onClick = {
                        searchQuery = "Telemarketing"
                        performSearch("Telemarketing")
                    },
                    label = { Text("?? Spam Numbers") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isSearching) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AegisBlue)
            }
        } else if (searchQuery.isNotEmpty()) {
            // Search Results List
            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonSearch, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching caller found for '$searchQuery'", fontWeight = FontWeight.SemiBold)
                        Text("Try entering the complete phone number with country code.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Text("Search Results (${searchResults.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                val clean = num.replace("[^0-9]".toRegex(), "")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$clean")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try { context.startActivity(intent) } catch (e: Exception) {}
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
                            }
                        )
                    }
                }
            }
        } else {
            // Default view: Recent Unknown Callers & Top Spammers
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text("Recent Unknown Numbers Who Called You", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tap any number to run reverse caller lookup", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = "Inspect", modifier = Modifier.size(16.dp))
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
}

@Composable
fun SearchResultCard(
    item: SearchResultItem,
    onCall: (String) -> Unit,
    onWhatsApp: (String) -> Unit,
    onBlock: (String) -> Unit,
    onAnnounce: (String, Boolean) -> Unit
) {
    val isSpam = item.riskLevel == SpamRiskLevel.HIGH_RISK_SPAM || item.riskLevel == SpamRiskLevel.FRAUD_SCAM
    val badgeColor = when (item.riskLevel) {
        SpamRiskLevel.SAFE -> AegisShieldGreen
        SpamRiskLevel.SUSPICIOUS -> AegisWarningOrange
        SpamRiskLevel.HIGH_RISK_SPAM, SpamRiskLevel.FRAUD_SCAM -> AegisDangerRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSpam) AegisDangerRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(badgeColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.displayName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = badgeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(item.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(item.phoneNumber, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isSpam) "?? SPAM" else "SAFE",
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details info
            Text(
                text = "Category: ${item.category} ? ${item.cityOrCarrier}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (item.callCount > 0) {
                Text(
                    text = "Interaction History: Called you ${item.callCount} times",
                    fontSize = 12.sp,
                    color = AegisBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onCall(item.phoneNumber) }) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = AegisShieldGreen)
                }

                IconButton(onClick = { onWhatsApp(item.phoneNumber) }) {
                    Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                }

                IconButton(onClick = { onAnnounce(item.displayName, isSpam) }) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Announce Name", tint = AegisBlue)
                }

                IconButton(onClick = { onBlock(item.phoneNumber) }) {
                    Icon(Icons.Default.Shield, contentDescription = "Block", tint = AegisDangerRed)
                }
            }
        }
    }
}
