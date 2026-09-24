package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DiscordCornerRadius = RoundedCornerShape(12.dp)

// Official Discord mobile palette
val DiscordDarkest = Color(0xFF1E1F22)       // Server rail & background
val DiscordDark = Color(0xFF2B2D31)          // Channel sidebar & cards
val DiscordChatBackground = Color(0xFF313338)// Main chat canvas
val DiscordInputBox = Color(0xFF383A40)      // Chat input
val DiscordHover = Color(0xFF35373C)
val DiscordActive = Color(0xFF404249)
val DiscordBlurple = Color(0xFF5865F2)       // Official Discord Blurple
val DiscordGreen = Color(0xFF23A55A)         // Online / Voice / Success
val DiscordRed = Color(0xFFF23F43)           // Mention / DND / Hangup
val DiscordYellow = Color(0xFFF0B232)        // Idle
val DiscordTextPrimary = Color(0xFFF2F3F5)
val DiscordTextMuted = Color(0xFF949BA4)
val DiscordBorder = Color(0xFF232428)
val DiscordNitroPink = Color(0xFFF47FFF)

/**
 * Official Discord Mobile Bottom Navigation Bar
 */
@Composable
fun DiscordBottomBar(
    currentTab: DiscordTab,
    onSelectTab: (DiscordTab) -> Unit,
    userPresence: UserPresence,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = DiscordDarkest,
        tonalElevation = 8.dp
    ) {
        DiscordTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Box(contentAlignment = Alignment.Center) {
                        if (tab == DiscordTab.YOU) {
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) DiscordBlurple else DiscordDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "😎", fontSize = 14.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(userPresence.colorHex))
                                        .border(1.dp, DiscordDarkest, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        } else {
                            Text(
                                text = tab.iconEmoji,
                                fontSize = if (isSelected) 20.sp else 18.sp
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DiscordBlurple,
                    selectedTextColor = Color.White,
                    unselectedIconColor = DiscordTextMuted,
                    unselectedTextColor = DiscordTextMuted,
                    indicatorColor = DiscordActive
                )
            )
        }
    }
}

/**
 * 1. Left-most Discord Server Rail
 */
@Composable
fun DiscordServerRail(
    servers: List<DiscordServer>,
    selectedServerId: String,
    isDirectMessages: Boolean,
    onSelectServer: (String) -> Unit,
    onSelectDirectMessages: () -> Unit,
    onAddServerClick: () -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(72.dp)
            .fillMaxHeight()
            .background(DiscordDarkest)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // DM Icon
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isDirectMessages) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(4.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .background(Color.White)
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(if (isDirectMessages) DiscordCornerRadius else CircleShape)
                    .background(if (isDirectMessages) DiscordBlurple else DiscordDark)
                    .clickable { onSelectDirectMessages() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💬", fontSize = 22.sp)
            }
        }

        // Pill separator
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(2.dp)
                .background(DiscordBorder)
        )

        // Server List
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(servers, key = { it.id }) { server ->
                val isSelected = !isDirectMessages && server.id == selectedServerId
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .width(4.dp)
                                .height(38.dp)
                                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                                .background(Color.White)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(if (isSelected) DiscordCornerRadius else CircleShape)
                            .background(if (isSelected) DiscordBlurple else DiscordDark)
                            .clickable { onSelectServer(server.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = server.iconEmoji, fontSize = 22.sp)

                        if (server.badgeCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(17.dp)
                                    .clip(CircleShape)
                                    .background(DiscordRed)
                                    .border(2.dp, DiscordDarkest, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${server.badgeCount}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Add Server Button (+)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DiscordDark)
                        .clickable { onAddServerClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Sunucu Ekle",
                        tint = DiscordGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            item {
                // Explore Servers Button (Compass)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DiscordDark)
                        .clickable { onExploreClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Sunucuları Keşfet",
                        tint = DiscordTextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * 2. Discord Channel Drawer
 */
@Composable
fun DiscordChannelDrawer(
    server: DiscordServer?,
    channels: List<DiscordChannel>,
    selectedChannelId: String,
    connectedVoiceChannelId: String?,
    isMicMuted: Boolean,
    isDeafened: Boolean,
    userName: String,
    userTag: String,
    userPresence: UserPresence,
    userCustomStatus: String,
    onSelectChannel: (String) -> Unit,
    onOpenVoiceOverlay: () -> Unit,
    onDisconnectVoice: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleDeafen: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddChannelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(DiscordDark)
    ) {
        // Server Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DiscordDark,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = server?.name ?: "Discord Sunucusu",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DiscordTextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "🛡️", fontSize = 12.sp)
                }

                IconButton(
                    onClick = onAddChannelClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Kanal Ekle",
                        tint = DiscordTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = DiscordBorder, thickness = 1.dp)

        val textChannels = channels.filter { it.type == ChannelType.TEXT }
        val voiceChannels = channels.filter { it.type == ChannelType.VOICE }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Text Channels Category
            item {
                Text(
                    text = "▼ METİN KANALLARI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DiscordTextMuted,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
                )
            }

            items(textChannels, key = { it.id }) { channel ->
                val isSelected = channel.id == selectedChannelId
                Surface(
                    onClick = { onSelectChannel(channel.id) },
                    shape = DiscordCornerRadius,
                    color = if (isSelected) DiscordActive else Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else DiscordTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = channel.name,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else DiscordTextMuted
                        )
                    }
                }
            }

            // Voice Channels Category
            item {
                Text(
                    text = "▼ SES KANALLARI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DiscordTextMuted,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 14.dp, bottom = 4.dp)
                )
            }

            items(voiceChannels, key = { it.id }) { channel ->
                val isConnected = channel.id == connectedVoiceChannelId
                Surface(
                    onClick = { onSelectChannel(channel.id) },
                    shape = DiscordCornerRadius,
                    color = if (isConnected) DiscordHover else Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = if (isConnected) DiscordGreen else DiscordTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = channel.name,
                            fontSize = 13.sp,
                            fontWeight = if (isConnected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isConnected) DiscordGreen else DiscordTextMuted
                        )
                    }
                }
            }
        }

        // Active Voice Status Bar
        if (connectedVoiceChannelId != null) {
            val connectedChannel = channels.find { it.id == connectedVoiceChannelId }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenVoiceOverlay() },
                color = Color(0xFF222428),
                border = BorderStroke(1.dp, DiscordBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(DiscordGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ses Bağlandı (RTC)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DiscordGreen
                            )
                        }
                        Text(
                            text = connectedChannel?.name ?: "Ses Odası",
                            fontSize = 11.sp,
                            color = DiscordTextMuted
                        )
                    }

                    IconButton(
                        onClick = onDisconnectVoice,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Ayrıl",
                            tint = DiscordRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // User Profile Dock at bottom
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DiscordDarkest,
            border = BorderStroke(1.dp, DiscordBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenSettings() }
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DiscordBlurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "😎", fontSize = 16.sp)
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(userPresence.colorHex))
                                .border(1.5.dp, DiscordDarkest, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "$userName$userTag",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DiscordTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = userCustomStatus,
                            fontSize = 10.sp,
                            color = DiscordTextMuted,
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleMic,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mikrofon",
                            tint = if (isMicMuted) DiscordRed else DiscordTextMuted,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleDeafen,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Kulaklık",
                            tint = if (isDeafened) DiscordRed else DiscordTextMuted,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = DiscordTextMuted,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. Discord Main Chat View
 */
@Composable
fun DiscordChatView(
    channel: DiscordChannel?,
    messages: List<DiscordMessage>,
    onSendMessage: (String) -> Unit,
    onToggleReaction: (String, String) -> Unit,
    onToggleMembers: () -> Unit,
    onToggleDrawer: () -> Unit,
    isMobile: Boolean,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DiscordChatBackground)
    ) {
        // Channel Top Header Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DiscordChatBackground,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, DiscordBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = isMobile) { onToggleDrawer() }
                ) {
                    if (isMobile) {
                        IconButton(
                            onClick = onToggleDrawer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Text(text = "☰", fontSize = 22.sp, color = DiscordTextPrimary)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = DiscordTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = channel?.name ?: "sohbet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DiscordTextPrimary
                        )
                        if (!channel?.topic.isNullOrBlank()) {
                            Text(
                                text = channel?.topic ?: "",
                                fontSize = 11.sp,
                                color = DiscordTextMuted,
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleMembers,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Üyeler",
                            tint = DiscordTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Messages Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(DiscordDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "#${channel?.name} kanalına hoş geldiniz!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = DiscordTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bu, #${channel?.name} kanalının başlangıcıdır. Mesaj göndererek sohbete katılın.",
                            fontSize = 13.sp,
                            color = DiscordTextMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = DiscordBorder)
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                DiscordMessageRow(
                    message = msg,
                    onReact = { emoji -> onToggleReaction(msg.id, emoji) }
                )
            }
        }

        // Message Input Box with attachment and emoji buttons
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            shape = DiscordCornerRadius,
            color = DiscordInputBox
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add attachment icon (+)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DiscordDark)
                        .clickable { textInput += " 📷 [Ekran_Goruntusu.png]" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ek Ekle",
                        tint = DiscordTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = "Mesaj gönder #${channel?.name ?: "sohbet"}",
                            fontSize = 14.sp,
                            color = DiscordTextMuted
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = DiscordTextPrimary,
                        unfocusedTextColor = DiscordTextPrimary
                    ),
                    singleLine = false,
                    maxLines = 3
                )

                // Emoji picker shortcut
                Text(
                    text = "😊",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { textInput += " ✨" }
                        .padding(4.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (textInput.isNotBlank()) DiscordBlurple else Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gönder",
                        tint = if (textInput.isNotBlank()) Color.White else DiscordTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Message Row Component
 */
@Composable
fun DiscordMessageRow(
    message: DiscordMessage,
    onReact: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(DiscordDark),
            contentAlignment = Alignment.Center
        ) {
            Text(text = message.avatarEmoji, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.senderName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(message.roleColorHex)
                )

                message.roleBadge?.let { badge ->
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(message.roleColorHex).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(message.roleColorHex))
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(message.roleColorHex),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = message.timestamp,
                    fontSize = 11.sp,
                    color = DiscordTextMuted
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = message.content,
                fontSize = 14.sp,
                color = DiscordTextPrimary,
                lineHeight = 20.sp
            )

            // Attachment Card
            message.attachment?.let { att ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = DiscordCornerRadius,
                    color = DiscordDark,
                    border = BorderStroke(1.dp, DiscordBorder),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = att.previewEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = att.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DiscordTextPrimary
                            )
                            Text(
                                text = "Görsel Dosyası • 2.4 MB",
                                fontSize = 10.sp,
                                color = DiscordTextMuted
                            )
                        }
                    }
                }
            }

            // Reactions Row
            if (message.reactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    message.reactions.forEach { reaction ->
                        Surface(
                            onClick = { onReact(reaction.emoji) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (reaction.userReacted) DiscordBlurple.copy(alpha = 0.3f) else DiscordDark,
                            border = BorderStroke(
                                1.dp,
                                if (reaction.userReacted) DiscordBlurple else DiscordBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = reaction.emoji, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${reaction.count}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (reaction.userReacted) DiscordBlurple else DiscordTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Quick emoji bar
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                listOf("👍", "❤️", "😂", "🔥").forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onReact(emoji) }
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}

/**
 * 4. Discord Voice Call Screen Overlay
 */
@Composable
fun DiscordVoiceCallOverlay(
    channelName: String,
    speakers: List<String>,
    isMuted: Boolean,
    isDeafened: Boolean,
    onToggleMic: () -> Unit,
    onToggleDeafen: () -> Unit,
    onDisconnect: () -> Unit,
    onCloseOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DiscordDarkest)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseOverlay) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Aşağı İndir",
                    tint = DiscordTextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = channelName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DiscordTextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(DiscordGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RTC Bağlandı (14ms)",
                        fontSize = 11.sp,
                        color = DiscordGreen
                    )
                }
            }

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Katılımcılar",
                    tint = DiscordTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Voice Grid (Active speakers with green pulsing outline)
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speaker 1 (Eren)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(DiscordDark)
                        .border(3.dp, DiscordGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👑", fontSize = 38.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Eren_Admin",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DiscordTextPrimary
                )
            }

            // Speaker 2 (Sen)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(DiscordDark)
                        .border(3.dp, if (!isMuted) DiscordGreen else DiscordDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "😎", fontSize = 38.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sen",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DiscordTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Call Control Bar
        Surface(
            shape = DiscordCornerRadius,
            color = DiscordDark,
            border = BorderStroke(1.dp, DiscordBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Button
                IconButton(
                    onClick = onToggleMic,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) DiscordRed else DiscordInputBox)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mikrofon",
                        tint = Color.White
                    )
                }

                // Deafen Button
                IconButton(
                    onClick = onToggleDeafen,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (isDeafened) DiscordRed else DiscordInputBox)
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Kulaklık",
                        tint = Color.White
                    )
                }

                // Disconnect Button
                IconButton(
                    onClick = onDisconnect,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(DiscordRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Ayrıl",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 5. Direct Messages View
 */
@Composable
fun DiscordDirectMessagesView(
    friends: List<DiscordFriend>,
    selectedFriendId: String,
    messages: List<DiscordMessage>,
    onSelectFriend: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeFriend = friends.find { it.id == selectedFriendId } ?: friends.firstOrNull()
    var dmInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DiscordChatBackground)
    ) {
        // DM Header Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DiscordChatBackground,
            border = BorderStroke(1.dp, DiscordBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DiscordBlurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = activeFriend?.avatarEmoji ?: "👤", fontSize = 18.sp)
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(activeFriend?.presence?.colorHex ?: 0xFF23A55A))
                                .border(1.dp, DiscordDarkest, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = activeFriend?.name ?: "Arkadaş",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DiscordTextPrimary
                        )
                        Text(
                            text = activeFriend?.activity ?: "Çevrimiçi",
                            fontSize = 11.sp,
                            color = DiscordTextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Sesli Arama", tint = DiscordTextMuted)
                    }
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Görüntülü Arama", tint = DiscordTextMuted)
                    }
                }
            }
        }

        // Friends Switcher Horizontal Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DiscordDark,
            border = BorderStroke(1.dp, DiscordBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                friends.forEach { friend ->
                    val isSelected = friend.id == selectedFriendId
                    Surface(
                        onClick = { onSelectFriend(friend.id) },
                        shape = DiscordCornerRadius,
                        color = if (isSelected) DiscordActive else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = friend.avatarEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = friend.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else DiscordTextMuted
                            )
                        }
                    }
                }
            }
        }

        // DM Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                DiscordMessageRow(message = msg, onReact = {})
            }
        }

        // Input
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            shape = DiscordCornerRadius,
            color = DiscordInputBox
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = dmInput,
                    onValueChange = { dmInput = it },
                    placeholder = {
                        Text(
                            text = "@${activeFriend?.name ?: "kullanıcı"} kişisine mesaj gönder",
                            fontSize = 14.sp,
                            color = DiscordTextMuted
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = DiscordTextPrimary,
                        unfocusedTextColor = DiscordTextPrimary
                    )
                )

                IconButton(
                    onClick = {
                        if (dmInput.isNotBlank()) {
                            onSendMessage(dmInput)
                            dmInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (dmInput.isNotBlank()) DiscordBlurple else Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gönder",
                        tint = if (dmInput.isNotBlank()) Color.White else DiscordTextMuted
                    )
                }
            }
        }
    }
}

/**
 * 6. Notifications Tab View
 */
@Composable
fun DiscordNotificationsView(
    notifications: List<DiscordNotification>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DiscordChatBackground)
            .padding(14.dp)
    ) {
        Text(
            text = "Bildirimler",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DiscordTextPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(notifications, key = { it.id }) { notif ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = DiscordCornerRadius,
                    colors = CardDefaults.cardColors(containerColor = DiscordDark),
                    border = BorderStroke(1.dp, DiscordBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DiscordBlurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = notif.iconEmoji, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = notif.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DiscordTextPrimary
                                )
                                Text(
                                    text = notif.timestamp,
                                    fontSize = 10.sp,
                                    color = DiscordTextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = notif.description,
                                fontSize = 12.sp,
                                color = DiscordTextMuted,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 7. Official Profile Tab ("Sen")
 */
@Composable
fun DiscordProfileView(
    name: String,
    tag: String,
    presence: UserPresence,
    customStatus: String,
    aboutMe: String,
    hasNitro: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DiscordChatBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = DiscordCornerRadius,
                colors = CardDefaults.cardColors(containerColor = DiscordDark),
                border = BorderStroke(1.dp, DiscordBorder)
            ) {
                Column {
                    // Profile Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(DiscordBlurple, Color(0xFF6C3483), DiscordNitroPink)
                                )
                            )
                    )

                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-36).dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(DiscordDarkest)
                                        .border(3.dp, DiscordDark, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "😎", fontSize = 34.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(presence.colorHex))
                                        .border(2.dp, DiscordDark, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }

                            Button(
                                onClick = onOpenSettings,
                                shape = DiscordCornerRadius,
                                colors = ButtonDefaults.buttonColors(containerColor = DiscordActive)
                            ) {
                                Text(text = "Profili Düzenle", fontSize = 12.sp, color = DiscordTextPrimary)
                            }
                        }

                        Text(
                            text = "$name$tag",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = DiscordTextPrimary
                        )

                        Text(
                            text = customStatus,
                            fontSize = 13.sp,
                            color = DiscordTextMuted
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = DiscordBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "HAKKIMDA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DiscordTextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = aboutMe,
                            fontSize = 13.sp,
                            color = DiscordTextPrimary
                        )
                    }
                }
            }
        }

        // Nitro Perks Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = DiscordCornerRadius,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1C38)),
                border = BorderStroke(1.dp, DiscordNitroPink.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🚀", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Discord Nitro",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DiscordNitroPink
                        )
                        Text(
                            text = "Özel emojiler, 500 MB dosya yükleme ve HD yayın aktif.",
                            fontSize = 12.sp,
                            color = DiscordTextPrimary
                        )
                    }
                }
            }
        }

        // App Settings List
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = DiscordCornerRadius,
                colors = CardDefaults.cardColors(containerColor = DiscordDark),
                border = BorderStroke(1.dp, DiscordBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val settingsItems = listOf(
                        "👤 Hesap Ayarları" to "Şifre, E-posta ve Güvenlik",
                        "🎨 Görünüm" to "Koyu Tema (Amoled)",
                        "🎙️ Ses ve Görüntü" to "Krisp Gürültü Engelleme Aktif",
                        "🔔 Bildirimler" to "Masaüstü ve Mobil Uyarıları",
                        "🔒 Gizlilik & Güvenlik" to "Direkt mesaj filtreleme"
                    )

                    settingsItems.forEach { (title, subtitle) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenSettings() }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DiscordTextPrimary)
                                Text(text = subtitle, fontSize = 11.sp, color = DiscordTextMuted)
                            }
                            Text(text = "›", fontSize = 18.sp, color = DiscordTextMuted)
                        }
                        HorizontalDivider(color = DiscordBorder)
                    }
                }
            }
        }
    }
}

/**
 * 8. Explore Servers Dialog
 */
@Composable
fun DiscordExploreServersDialog(
    exploreServers: List<DiscordServer>,
    onJoinServer: (DiscordServer) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DiscordCornerRadius,
        containerColor = DiscordDark,
        title = {
            Text(
                text = "🧭 Keşfet: Popüler Sunucular",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DiscordTextPrimary
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(exploreServers, key = { it.id }) { srv ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = DiscordCornerRadius,
                        colors = CardDefaults.cardColors(containerColor = DiscordDarkest),
                        border = BorderStroke(1.dp, DiscordBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = srv.iconEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = srv.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DiscordTextPrimary)
                                    Text(text = "👥 ${srv.memberCount} Üye", fontSize = 11.sp, color = DiscordTextMuted)
                                }
                            }

                            Button(
                                onClick = { onJoinServer(srv) },
                                shape = DiscordCornerRadius,
                                colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple)
                            ) {
                                Text(text = "Katıl", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Kapat", color = DiscordTextMuted)
            }
        }
    )
}

/**
 * 9. Right Members Drawer
 */
@Composable
fun DiscordMembersDrawer(
    members: List<DiscordMember>,
    onSelectMember: (DiscordMember) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(DiscordDark)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ÜYELER — ${members.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DiscordTextMuted
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = DiscordTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val onlineMembers = members.filter { it.presence != UserPresence.OFFLINE }
        val offlineMembers = members.filter { it.presence == UserPresence.OFFLINE }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item {
                Text(
                    text = "ÇEVRİMİÇİ — ${onlineMembers.size}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DiscordTextMuted,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(onlineMembers, key = { it.id }) { member ->
                MemberItemRow(member = member, onClick = { onSelectMember(member) })
            }

            if (offlineMembers.isNotEmpty()) {
                item {
                    Text(
                        text = "ÇEVRİMDIŞI — ${offlineMembers.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DiscordTextMuted,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }

                items(offlineMembers, key = { it.id }) { member ->
                    MemberItemRow(member = member, onClick = { onSelectMember(member) })
                }
            }
        }
    }
}

@Composable
fun MemberItemRow(
    member: DiscordMember,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = DiscordCornerRadius,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DiscordDarkest),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = member.avatarEmoji, fontSize = 16.sp)
                }
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(member.presence.colorHex))
                        .border(1.dp, DiscordDark, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = member.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(member.roleColorHex),
                    maxLines = 1
                )
                Text(
                    text = member.customStatus,
                    fontSize = 11.sp,
                    color = DiscordTextMuted,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 10. Dialogs
 */
@Composable
fun DiscordCreateServerDialog(
    onDismiss: () -> Unit,
    onCreateServer: (name: String, emoji: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🌟") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DiscordCornerRadius,
        containerColor = DiscordDark,
        title = {
            Text(
                text = "Sunucunu Oluştur",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DiscordTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Text(
                    text = "Yeni sunucuna bir isim ve simge vererek arkadaşlarınla sohbet et.",
                    fontSize = 13.sp,
                    color = DiscordTextMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "SUNUCU ADI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DiscordTextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Örn: Oyun Kulübü", fontSize = 13.sp, color = DiscordTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = DiscordCornerRadius,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DiscordDarkest,
                        unfocusedContainerColor = DiscordDarkest,
                        focusedBorderColor = DiscordBlurple,
                        unfocusedBorderColor = DiscordBorder,
                        focusedTextColor = DiscordTextPrimary,
                        unfocusedTextColor = DiscordTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "SİMGE (EMOJI)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DiscordTextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = DiscordCornerRadius,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DiscordDarkest,
                        unfocusedContainerColor = DiscordDarkest,
                        focusedBorderColor = DiscordBlurple,
                        unfocusedBorderColor = DiscordBorder,
                        focusedTextColor = DiscordTextPrimary,
                        unfocusedTextColor = DiscordTextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateServer(name, emoji) },
                shape = DiscordCornerRadius,
                colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple)
            ) {
                Text(text = "Oluştur", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "İptal", color = DiscordTextMuted)
            }
        }
    )
}

@Composable
fun DiscordCreateChannelDialog(
    onDismiss: () -> Unit,
    onCreateChannel: (name: String, type: ChannelType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var channelType by remember { mutableStateOf(ChannelType.TEXT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DiscordCornerRadius,
        containerColor = DiscordDark,
        title = {
            Text(
                text = "Kanal Oluştur",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DiscordTextPrimary
            )
        },
        text = {
            Column {
                Text(text = "KANAL TÜRÜ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DiscordTextMuted)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(DiscordCornerRadius)
                        .background(if (channelType == ChannelType.TEXT) DiscordActive else DiscordDarkest)
                        .clickable { channelType = ChannelType.TEXT }
                        .padding(8.dp)
                ) {
                    RadioButton(
                        selected = channelType == ChannelType.TEXT,
                        onClick = { channelType = ChannelType.TEXT },
                        colors = RadioButtonDefaults.colors(selectedColor = DiscordBlurple)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = "Metin Kanalı", fontWeight = FontWeight.Bold, color = DiscordTextPrimary, fontSize = 13.sp)
                        Text(text = "Mesajlar ve görseller", color = DiscordTextMuted, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(DiscordCornerRadius)
                        .background(if (channelType == ChannelType.VOICE) DiscordActive else DiscordDarkest)
                        .clickable { channelType = ChannelType.VOICE }
                        .padding(8.dp)
                ) {
                    RadioButton(
                        selected = channelType == ChannelType.VOICE,
                        onClick = { channelType = ChannelType.VOICE },
                        colors = RadioButtonDefaults.colors(selectedColor = DiscordBlurple)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = "Ses Kanalı", fontWeight = FontWeight.Bold, color = DiscordTextPrimary, fontSize = 13.sp)
                        Text(text = "Sesli sohbet", color = DiscordTextMuted, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "KANAL ADI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DiscordTextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("sohbet", fontSize = 13.sp, color = DiscordTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = DiscordCornerRadius,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DiscordDarkest,
                        unfocusedContainerColor = DiscordDarkest,
                        focusedBorderColor = DiscordBlurple,
                        unfocusedBorderColor = DiscordBorder,
                        focusedTextColor = DiscordTextPrimary,
                        unfocusedTextColor = DiscordTextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateChannel(name, channelType) },
                shape = DiscordCornerRadius,
                colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple)
            ) {
                Text(text = "Oluştur", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "İptal", color = DiscordTextMuted)
            }
        }
    )
}

@Composable
fun DiscordUserSettingsDialog(
    currentName: String,
    currentPresence: UserPresence,
    currentStatus: String,
    currentAboutMe: String,
    onDismiss: () -> Unit,
    onSave: (name: String, presence: UserPresence, status: String, aboutMe: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var presence by remember { mutableStateOf(currentPresence) }
    var status by remember { mutableStateOf(currentStatus) }
    var aboutMe by remember { mutableStateOf(currentAboutMe) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DiscordCornerRadius,
        containerColor = DiscordDark,
        title = {
            Text(
                text = "Kullanıcı Profili ve Durum",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DiscordTextPrimary
            )
        },
        text = {
            Column {
                Text(text = "GÖRÜNEN AD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DiscordTextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = DiscordCornerRadius,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DiscordDarkest,
                        unfocusedContainerColor = DiscordDarkest,
                        focusedBorderColor = DiscordBlurple,
                        unfocusedBorderColor = DiscordBorder,
                        focusedTextColor = DiscordTextPrimary,
                        unfocusedTextColor = DiscordTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "ÖZEL DURUM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DiscordTextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = DiscordCornerRadius,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DiscordDarkest,
                        unfocusedContainerColor = DiscordDarkest,
                        focusedBorderColor = DiscordBlurple,
                        unfocusedBorderColor = DiscordBorder,
                        focusedTextColor = DiscordTextPrimary,
                        unfocusedTextColor = DiscordTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "HAKKIMDA BİYOGRAFİSİ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DiscordTextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = aboutMe,
                    onValueChange = { aboutMe = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = DiscordCornerRadius,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DiscordDarkest,
                        unfocusedContainerColor = DiscordDarkest,
                        focusedBorderColor = DiscordBlurple,
                        unfocusedBorderColor = DiscordBorder,
                        focusedTextColor = DiscordTextPrimary,
                        unfocusedTextColor = DiscordTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "ÇEVRİMİÇİ DURUMU", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DiscordTextMuted)
                Spacer(modifier = Modifier.height(6.dp))

                UserPresence.entries.forEach { p ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { presence = p }
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(p.colorHex))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = p.title,
                            fontSize = 13.sp,
                            fontWeight = if (presence == p) FontWeight.Bold else FontWeight.Normal,
                            color = if (presence == p) Color.White else DiscordTextPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, presence, status, aboutMe) },
                shape = DiscordCornerRadius,
                colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple)
            ) {
                Text(text = "Kaydet", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "İptal", color = DiscordTextMuted)
            }
        }
    )
}

@Composable
fun DiscordMemberProfileDialog(
    member: DiscordMember,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DiscordCornerRadius,
        containerColor = DiscordDark,
        title = null,
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(DiscordBlurple, Color(0xFF3C4270))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(DiscordDarkest)
                                .border(3.dp, DiscordDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = member.avatarEmoji, fontSize = 28.sp)
                        }
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(member.presence.colorHex))
                                .border(2.dp, DiscordDark, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .offset(y = (-14).dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "${member.name}${member.tag}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = DiscordTextPrimary
                    )
                    Text(
                        text = member.customStatus,
                        fontSize = 12.sp,
                        color = DiscordTextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = DiscordBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "ROLLER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DiscordTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(member.roleColorHex).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(member.roleColorHex))
                    ) {
                        Text(
                            text = member.role,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(member.roleColorHex),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "KATILMA TARİHİ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DiscordTextMuted)
                    Text(text = member.joinDate, fontSize = 12.sp, color = DiscordTextPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = DiscordCornerRadius,
                colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Kapat", fontWeight = FontWeight.Bold)
            }
        }
    )
}
