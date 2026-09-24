package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FloppiaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FloppiaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloppiaTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val uiState by viewModel.uiState.collectAsState()

                LaunchedEffect(uiState.toastMessage) {
                    uiState.toastMessage?.let { msg ->
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearToast()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DiscordDarkest,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        // Discord Mobile Bottom Bar
                        if (!uiState.isVoiceOverlayOpen) {
                            DiscordBottomBar(
                                currentTab = uiState.currentTab,
                                onSelectTab = { tab ->
                                    if (tab == DiscordTab.SERVERS && uiState.currentTab == DiscordTab.SERVERS) {
                                        viewModel.toggleServerDrawer()
                                    } else {
                                        viewModel.selectTab(tab)
                                    }
                                },
                                userPresence = uiState.myPresence
                            )
                        }
                    }
                ) { innerPadding ->
                    DiscordAppRoot(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DiscordAppRoot(
    viewModel: FloppiaViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Modals
    if (uiState.showCreateServerDialog) {
        DiscordCreateServerDialog(
            onDismiss = { viewModel.showCreateServerDialog(false) },
            onCreateServer = { name, emoji -> viewModel.createServer(name, emoji) }
        )
    }

    if (uiState.showCreateChannelDialog) {
        DiscordCreateChannelDialog(
            onDismiss = { viewModel.showCreateChannelDialog(false) },
            onCreateChannel = { name, type -> viewModel.createChannel(name, type) }
        )
    }

    if (uiState.showUserSettingsDialog) {
        DiscordUserSettingsDialog(
            currentName = uiState.myName,
            currentPresence = uiState.myPresence,
            currentStatus = uiState.myCustomStatus,
            currentAboutMe = uiState.myAboutMe,
            onDismiss = { viewModel.showUserSettingsDialog(false) },
            onSave = { name, presence, status, aboutMe ->
                viewModel.updateProfile(name, presence, status, aboutMe)
            }
        )
    }

    if (uiState.showExploreServersDialog) {
        DiscordExploreServersDialog(
            exploreServers = uiState.publicExploreServers,
            onJoinServer = { viewModel.joinExploreServer(it) },
            onDismiss = { viewModel.showExploreServersDialog(false) }
        )
    }

    uiState.inspectedMember?.let { member ->
        DiscordMemberProfileDialog(
            member = member,
            onDismiss = { viewModel.inspectMember(null) }
        )
    }

    // Fullscreen Voice Overlay
    if (uiState.isVoiceOverlayOpen) {
        val connectedChannel = uiState.channels.find { it.id == uiState.connectedVoiceChannelId }
        DiscordVoiceCallOverlay(
            channelName = connectedChannel?.name ?: "Ses Odası",
            speakers = uiState.activeSpeakers,
            isMuted = uiState.isMicMuted,
            isDeafened = uiState.isDeafened,
            onToggleMic = { viewModel.toggleMicMute() },
            onToggleDeafen = { viewModel.toggleDeafen() },
            onDisconnect = { viewModel.disconnectVoice() },
            onCloseOverlay = { viewModel.toggleVoiceOverlay() },
            modifier = modifier
        )
        return
    }

    // Tab Router
    when (uiState.currentTab) {
        DiscordTab.SERVERS -> {
            DiscordServersTabScreen(viewModel = viewModel, modifier = modifier)
        }
        DiscordTab.MESSAGES -> {
            DiscordDirectMessagesView(
                friends = uiState.friends,
                selectedFriendId = uiState.selectedFriendId,
                messages = uiState.dmMessagesByFriend[uiState.selectedFriendId] ?: emptyList(),
                onSelectFriend = { viewModel.selectFriend(it) },
                onSendMessage = { viewModel.sendMessage(it) },
                modifier = modifier
            )
        }
        DiscordTab.NOTIFICATIONS -> {
            DiscordNotificationsView(
                notifications = uiState.notifications,
                modifier = modifier
            )
        }
        DiscordTab.YOU -> {
            DiscordProfileView(
                name = uiState.myName,
                tag = uiState.myTag,
                presence = uiState.myPresence,
                customStatus = uiState.myCustomStatus,
                aboutMe = uiState.myAboutMe,
                hasNitro = uiState.hasNitro,
                onOpenSettings = { viewModel.showUserSettingsDialog(true) },
                modifier = modifier
            )
        }
    }
}

@Composable
fun DiscordServersTabScreen(
    viewModel: FloppiaViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentServer = uiState.servers.find { it.id == uiState.selectedServerId }
    val currentServerChannels = uiState.channels.filter { it.serverId == uiState.selectedServerId }
    val currentChannel = uiState.channels.find { it.id == uiState.selectedChannelId }
    val currentMessages = uiState.messagesByChannel[uiState.selectedChannelId] ?: emptyList()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DiscordDarkest)
    ) {
        val isTablet = maxWidth >= 760.dp

        if (isTablet) {
            // Tablet & Desktop 3-pane Layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Server Rail (Left)
                DiscordServerRail(
                    servers = uiState.servers,
                    selectedServerId = uiState.selectedServerId,
                    isDirectMessages = false,
                    onSelectServer = { viewModel.selectServer(it) },
                    onSelectDirectMessages = { viewModel.selectTab(DiscordTab.MESSAGES) },
                    onAddServerClick = { viewModel.showCreateServerDialog(true) },
                    onExploreClick = { viewModel.showExploreServersDialog(true) }
                )

                // Channels Drawer
                DiscordChannelDrawer(
                    server = currentServer,
                    channels = currentServerChannels,
                    selectedChannelId = uiState.selectedChannelId,
                    connectedVoiceChannelId = uiState.connectedVoiceChannelId,
                    isMicMuted = uiState.isMicMuted,
                    isDeafened = uiState.isDeafened,
                    userName = uiState.myName,
                    userTag = uiState.myTag,
                    userPresence = uiState.myPresence,
                    userCustomStatus = uiState.myCustomStatus,
                    onSelectChannel = { viewModel.selectChannel(it) },
                    onOpenVoiceOverlay = { viewModel.toggleVoiceOverlay() },
                    onDisconnectVoice = { viewModel.disconnectVoice() },
                    onToggleMic = { viewModel.toggleMicMute() },
                    onToggleDeafen = { viewModel.toggleDeafen() },
                    onOpenSettings = { viewModel.showUserSettingsDialog(true) },
                    onAddChannelClick = { viewModel.showCreateChannelDialog(true) },
                    modifier = Modifier.width(230.dp)
                )

                // Main Chat Canvas
                DiscordChatView(
                    channel = currentChannel,
                    messages = currentMessages,
                    onSendMessage = { viewModel.sendMessage(it) },
                    onToggleReaction = { msgId, emoji -> viewModel.toggleReaction(msgId, emoji) },
                    onToggleMembers = { viewModel.toggleMembersDrawer() },
                    onToggleDrawer = {},
                    isMobile = false,
                    modifier = Modifier.weight(1f)
                )

                // Members Drawer (Desktop)
                if (uiState.isMembersDrawerOpen) {
                    DiscordMembersDrawer(
                        members = uiState.members,
                        onSelectMember = { viewModel.inspectMember(it) },
                        onClose = { viewModel.toggleMembersDrawer() }
                    )
                }
            }
        } else {
            // Mobile Layout with swipe drawers
            BackHandler(enabled = uiState.isServerDrawerOpen || uiState.isMembersDrawerOpen) {
                if (uiState.isServerDrawerOpen) viewModel.toggleServerDrawer()
                if (uiState.isMembersDrawerOpen) viewModel.toggleMembersDrawer()
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Main Chat Canvas
                DiscordChatView(
                    channel = currentChannel,
                    messages = currentMessages,
                    onSendMessage = { viewModel.sendMessage(it) },
                    onToggleReaction = { msgId, emoji -> viewModel.toggleReaction(msgId, emoji) },
                    onToggleMembers = { viewModel.toggleMembersDrawer() },
                    onToggleDrawer = { viewModel.toggleServerDrawer() },
                    isMobile = true,
                    modifier = Modifier.fillMaxSize()
                )

                // Left Slide Drawer (Server list + Channels)
                AnimatedVisibility(
                    visible = uiState.isServerDrawerOpen,
                    enter = slideInHorizontally { -it },
                    exit = slideOutHorizontally { -it },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.65f))
                                .clickable { viewModel.toggleServerDrawer() }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(min = 280.dp, max = 320.dp)
                                .fillMaxWidth(0.85f)
                        ) {
                            DiscordServerRail(
                                servers = uiState.servers,
                                selectedServerId = uiState.selectedServerId,
                                isDirectMessages = false,
                                onSelectServer = { viewModel.selectServer(it) },
                                onSelectDirectMessages = {
                                    viewModel.selectTab(DiscordTab.MESSAGES)
                                    viewModel.toggleServerDrawer()
                                },
                                onAddServerClick = { viewModel.showCreateServerDialog(true) },
                                onExploreClick = { viewModel.showExploreServersDialog(true) }
                            )

                            DiscordChannelDrawer(
                                server = currentServer,
                                channels = currentServerChannels,
                                selectedChannelId = uiState.selectedChannelId,
                                connectedVoiceChannelId = uiState.connectedVoiceChannelId,
                                isMicMuted = uiState.isMicMuted,
                                isDeafened = uiState.isDeafened,
                                userName = uiState.myName,
                                userTag = uiState.myTag,
                                userPresence = uiState.myPresence,
                                userCustomStatus = uiState.myCustomStatus,
                                onSelectChannel = { viewModel.selectChannel(it) },
                                onOpenVoiceOverlay = { viewModel.toggleVoiceOverlay() },
                                onDisconnectVoice = { viewModel.disconnectVoice() },
                                onToggleMic = { viewModel.toggleMicMute() },
                                onToggleDeafen = { viewModel.toggleDeafen() },
                                onOpenSettings = { viewModel.showUserSettingsDialog(true) },
                                onAddChannelClick = { viewModel.showCreateChannelDialog(true) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Right Slide Drawer (Members list)
                AnimatedVisibility(
                    visible = uiState.isMembersDrawerOpen,
                    enter = slideInHorizontally { it },
                    exit = slideOutHorizontally { it },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.65f))
                                .clickable { viewModel.toggleMembersDrawer() }
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .widthIn(min = 240.dp, max = 280.dp)
                                .fillMaxWidth(0.75f)
                        ) {
                            DiscordMembersDrawer(
                                members = uiState.members,
                                onSelectMember = { viewModel.inspectMember(it) },
                                onClose = { viewModel.toggleMembersDrawer() }
                            )
                        }
                    }
                }
            }
        }
    }
}
