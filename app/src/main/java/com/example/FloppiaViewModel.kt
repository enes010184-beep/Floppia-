package com.example

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class DiscordTab(val title: String, val iconEmoji: String) {
    SERVERS("Sunucular", "💬"),
    MESSAGES("Mesajlar", "✉️"),
    NOTIFICATIONS("Bildirimler", "🔔"),
    YOU("Sen", "👤")
}

enum class UserPresence(val title: String, val colorHex: Long) {
    ONLINE("Çevrimiçi", 0xFF23A55A),
    IDLE("Boşta", 0xFFF0B232),
    DND("Rahatsız Etmeyin", 0xFFF23F43),
    OFFLINE("Görünmez", 0xFF80848E)
}

enum class ChannelType {
    TEXT,
    VOICE
}

data class DiscordReaction(
    val emoji: String,
    val count: Int,
    val userReacted: Boolean = false
)

data class DiscordAttachment(
    val title: String,
    val type: String = "IMAGE",
    val previewEmoji: String = "🖼️"
)

data class DiscordMessage(
    val id: String = UUID.randomUUID().toString(),
    val channelId: String,
    val senderName: String,
    val roleBadge: String? = null,
    val roleColorHex: Long = 0xFF5865F2,
    val avatarEmoji: String = "👤",
    val content: String,
    val timestamp: String,
    val attachment: DiscordAttachment? = null,
    val reactions: List<DiscordReaction> = emptyList()
)

data class DiscordChannel(
    val id: String,
    val serverId: String,
    val name: String,
    val type: ChannelType,
    val topic: String = "",
    val category: String = "METİN KANALLARI",
    val unread: Boolean = false
)

data class DiscordServer(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val description: String = "",
    val badgeCount: Int = 0,
    val memberCount: Int = 42
)

data class DiscordMember(
    val id: String,
    val name: String,
    val tag: String,
    val avatarEmoji: String,
    val role: String,
    val roleColorHex: Long,
    val presence: UserPresence = UserPresence.ONLINE,
    val customStatus: String = "Discord kullanıyor",
    val joinDate: String = "Mart 2024"
)

data class DiscordFriend(
    val id: String,
    val name: String,
    val tag: String,
    val avatarEmoji: String,
    val presence: UserPresence,
    val customStatus: String,
    val activity: String = "",
    val lastMessage: String = "",
    val unreadCount: Int = 0
)

data class DiscordNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val timestamp: String,
    val iconEmoji: String = "📢",
    val read: Boolean = false
)

data class DiscordUiState(
    // Bottom Tab Bar
    val currentTab: DiscordTab = DiscordTab.SERVERS,

    // Navigation
    val isDirectMessages: Boolean = false,
    val selectedServerId: String = "floppia",
    val selectedChannelId: String = "c_genel",
    val selectedFriendId: String = "f1",

    // Active Voice State
    val connectedVoiceChannelId: String? = null,
    val isVoiceOverlayOpen: Boolean = false,
    val isMicMuted: Boolean = false,
    val isDeafened: Boolean = false,
    val activeSpeakers: List<String> = emptyList(),

    // User Profile
    val myName: String = "Enes",
    val myTag: String = "#1337",
    val myPresence: UserPresence = UserPresence.ONLINE,
    val myCustomStatus: String = "Discord'da sohbet ediyor...",
    val myAboutMe: String = "Android geliştirici & Minecraft oyuncusu. Her zaman aktif!",
    val myBannerColorHex: Long = 0xFF5865F2,
    val hasNitro: Boolean = true,

    // Drawer states (mobile)
    val isServerDrawerOpen: Boolean = false,
    val isMembersDrawerOpen: Boolean = false,

    // Modals
    val showCreateServerDialog: Boolean = false,
    val showCreateChannelDialog: Boolean = false,
    val showUserSettingsDialog: Boolean = false,
    val showExploreServersDialog: Boolean = false,
    val inspectedMember: DiscordMember? = null,

    // Data collections
    val servers: List<DiscordServer> = emptyList(),
    val publicExploreServers: List<DiscordServer> = emptyList(),
    val channels: List<DiscordChannel> = emptyList(),
    val messagesByChannel: Map<String, List<DiscordMessage>> = emptyMap(),
    val dmMessagesByFriend: Map<String, List<DiscordMessage>> = emptyMap(),
    val members: List<DiscordMember> = emptyList(),
    val friends: List<DiscordFriend> = emptyList(),
    val notifications: List<DiscordNotification> = emptyList(),

    // Transient notifications
    val toastMessage: String? = null,
    val isTyping: Boolean = false
)

class FloppiaViewModel(application: Application) : AndroidViewModel(application) {

    val soundEngine = SoundEngine(application)
    private val _uiState = MutableStateFlow(createInitialState())
    val uiState = _uiState.asStateFlow()

    private fun createInitialState(): DiscordUiState {
        val servers = listOf(
            DiscordServer("floppia", "Floppia Türkiye", "⛏️", "Resmi Minecraft & Oyun Topluluğu", 2, 1420),
            DiscordServer("gaming", "Oyun & Sohbet", "🎮", "Genel oyuncu buluşma noktası", 0, 890),
            DiscordServer("coding", "Yazılım & Kodlama", "💻", "Yazılımcılar ve geliştiriciler", 1, 560),
            DiscordServer("music", "Lofi & Müzik Odası", "🎧", "Sohbet ve müzik dinleme odası", 0, 310)
        )

        val exploreServers = listOf(
            DiscordServer("exp_mc", "Minecraft Türkiye", "🟩", "Türkiye'nin en büyük Minecraft topluluğu", 0, 142500),
            DiscordServer("exp_val", "Valorant TR", "🎯", "Takım bulma, turnuvalar ve sohbet", 0, 95400),
            DiscordServer("exp_dev", "Android & Kotlin Geliştiricileri", "🤖", "Mobil geliştiriciler ve kod paylaşımı", 0, 48200),
            DiscordServer("exp_anime", "Anime & Manga Kulübü", "🌸", "Dizi, film ve sohbet odaları", 0, 36000)
        )

        val channels = listOf(
            // Floppia channels (Pure human communication - NO BOTS)
            DiscordChannel("c_genel", "floppia", "💬・genel-sohbet", ChannelType.TEXT, "Herkesle genel sohbet kanalı", "METİN KANALLARI"),
            DiscordChannel("c_duyuru", "floppia", "📢・duyurular", ChannelType.TEXT, "Sunucu ve etkinlik duyuruları", "BİLGİ"),
            DiscordChannel("c_oyun", "floppia", "🎮・oyun-sohbet", ChannelType.TEXT, "Oyunlar ve sunucu muhabbeti", "METİN KANALLARI"),
            DiscordChannel("c_medya", "floppia", "📸・ekran-görüntüleri", ChannelType.TEXT, "Ekran görüntüleri ve mimari yapılar", "METİN KANALLARI"),
            DiscordChannel("c_oneriler", "floppia", "💡・öneriler", ChannelType.TEXT, "Sunucu geliştirme fikirleri ve öneriler", "TOPLULUK"),
            DiscordChannel("v_genel", "floppia", "🔊 Genel Ses Odası", ChannelType.VOICE, "Sohbet için serbest ses odası", "SES KANALLARI"),
            DiscordChannel("v_oyun", "floppia", "🔊 Oyun Odası (SQUAD)", ChannelType.VOICE, "Takımlı oyun ses kanalı", "SES KANALLARI"),

            // Gaming channels
            DiscordChannel("g_genel", "gaming", "💬・sohbet", ChannelType.TEXT, "Oyun tartışmaları", "METİN KANALLARI"),
            DiscordChannel("g_ses", "gaming", "🔊 Takım Odası", ChannelType.VOICE, "Ses kanalı", "SES KANALLARI"),

            // Coding channels
            DiscordChannel("k_yardim", "coding", "💻・kod-yardım", ChannelType.TEXT, "Kotlin, Android ve Web soruları", "YAZILIM"),
            DiscordChannel("k_projeler", "coding", "🚀・projeler", ChannelType.TEXT, "Projelerinizi paylaşın", "YAZILIM")
        )

        val initialMessages = mapOf(
            "c_genel" to listOf(
                DiscordMessage(
                    channelId = "c_genel",
                    senderName = "Eren_Admin",
                    roleBadge = "YÖNETİCİ",
                    roleColorHex = 0xFFED4245,
                    avatarEmoji = "👑",
                    content = "Selam herkese! Sunucumuza hoş geldiniz. Ses odalarımız, rollerimiz ve reaksiyonlarımız aktif! 🔥",
                    timestamp = "Bugün 10:14",
                    reactions = listOf(DiscordReaction("🔥", 5, true))
                ),
                DiscordMessage(
                    channelId = "c_genel",
                    senderName = "Berk",
                    roleBadge = "MOD",
                    roleColorHex = 0xFF57F287,
                    avatarEmoji = "🛡️",
                    content = "Hoş geldiniz arkadaşlar! Sol alttan mikrofonunuzu kapatabilir veya durumunuzu değiştirebilirsiniz.",
                    timestamp = "Bugün 10:22",
                    reactions = listOf(DiscordReaction("👍", 3, false))
                ),
                DiscordMessage(
                    channelId = "c_genel",
                    senderName = "Ayşe_Gamer",
                    roleBadge = "ÜYE",
                    roleColorHex = 0xFFEB459E,
                    avatarEmoji = "🎮",
                    content = "Akşam sesli sohbette oyun partisi var mı? 🎧",
                    timestamp = "Bugün 10:35"
                ),
                DiscordMessage(
                    channelId = "c_genel",
                    senderName = "Emre_Pro",
                    roleBadge = "KLAN LİDERİ",
                    roleColorHex = 0xFFFEE75C,
                    avatarEmoji = "⚔️",
                    content = "Bizim klan akşam 20:00'de toplanıyor, katılmak isteyen varsa yazsın!",
                    timestamp = "Bugün 10:48",
                    reactions = listOf(DiscordReaction("⚔️", 4, false))
                )
            ),
            "c_duyuru" to listOf(
                DiscordMessage(
                    channelId = "c_duyuru",
                    senderName = "Eren_Admin",
                    roleBadge = "YÖNETİCİ",
                    roleColorHex = 0xFFED4245,
                    avatarEmoji = "👑",
                    content = "📢 **BÜYÜK DUYURU**: Discord mobil arayüzümüz birebir Discord tasarımına kavuştu! Tabletlerde 3 sütunlu, telefonlarda kayan menülü olarak kusursuz çalışıyor.",
                    timestamp = "Dün 20:30",
                    reactions = listOf(DiscordReaction("🎉", 9, true), DiscordReaction("🚀", 4, false))
                )
            ),
            "c_medya" to listOf(
                DiscordMessage(
                    channelId = "c_medya",
                    senderName = "MimarAli",
                    roleBadge = "MİMAR",
                    roleColorHex = 0xFFFEE75C,
                    avatarEmoji = "🏰",
                    content = "Yeni kale tasarımım bitti, nasıl olmuş? 🏛️",
                    timestamp = "Dün 21:15",
                    attachment = DiscordAttachment("Ortaçağ_Kale_Spawn.png", "IMAGE", "🏰"),
                    reactions = listOf(DiscordReaction("🔥", 6, false))
                )
            ),
            "c_oneriler" to listOf(
                DiscordMessage(
                    channelId = "c_oneriler",
                    senderName = "Ayşe_Gamer",
                    roleBadge = "ÜYE",
                    roleColorHex = 0xFFEB459E,
                    avatarEmoji = "🎮",
                    content = "Hafta sonları klan turnuvaları düzenlensin!",
                    timestamp = "Bugün 09:15",
                    reactions = listOf(DiscordReaction("👍", 8, false))
                )
            )
        )

        // Real human members only - NO BOTS
        val members = listOf(
            DiscordMember("m1", "Eren_Admin", "#0001", "👑", "Yönetici", 0xFFED4245, UserPresence.ONLINE, "Sunucu Yönetimi"),
            DiscordMember("m2", "Berk", "#1024", "🛡️", "Moderatör", 0xFF57F287, UserPresence.ONLINE, "Sohbet Düzeni"),
            DiscordMember("m3", "Ayşe_Gamer", "#4040", "🎮", "Aktif Oyuncu", 0xFFEB459E, UserPresence.IDLE, "Minecraft oynuyor"),
            DiscordMember("m4", "MimarAli", "#7777", "🏰", "Mimar", 0xFFFEE75C, UserPresence.ONLINE, "Harita inşa ediyor..."),
            DiscordMember("m5", "Emre_Pro", "#5555", "⚔️", "Klan Lideri", 0xFFFEE75C, UserPresence.DND, "Turnuvada maç yapıyor"),
            DiscordMember("m6", "Can", "#3333", "😎", "Üye", 0xFF80848E, UserPresence.OFFLINE, "Çevrimdışı")
        )

        // Real friends only - NO BOTS
        val friends = listOf(
            DiscordFriend("f1", "Eren", "#0001", "👑", UserPresence.ONLINE, "Müsait", "Minecraft: Skyblock oynuyor", "Akşam Discord'da toplanıyoruz", 0),
            DiscordFriend("f2", "Ayşe", "#4040", "🎮", UserPresence.IDLE, "Spotify dinliyor", "Billie Eilish - Birds of a Feather", "Görüşürüz kanka", 1),
            DiscordFriend("f3", "Berk", "#1024", "🛡️", UserPresence.ONLINE, "Moderatörlük yapıyor", "VS Code: Kotlin Projesi", "Tamamdır kontrol ettim", 0),
            DiscordFriend("f4", "Emre", "#5555", "⚔️", UserPresence.DND, "Klan Maçında", "Turnuva 20:00'de", "Takımı topluyoruz", 0)
        )

        val dmMessages = mapOf(
            "f1" to listOf(
                DiscordMessage(
                    channelId = "dm_f1",
                    senderName = "Eren",
                    roleColorHex = 0xFFED4245,
                    avatarEmoji = "👑",
                    content = "Selam! Yeni Discord arayüzü tam orijinal gibi olmuş!",
                    timestamp = "Bugün 10:05"
                ),
                DiscordMessage(
                    channelId = "dm_f1",
                    senderName = "Sen",
                    roleColorHex = 0xFF5865F2,
                    avatarEmoji = "😎",
                    content = "Evet, ses kanalları, arkadaşlar ve sunucu listesi birebir çalışıyor!",
                    timestamp = "Bugün 10:07"
                )
            ),
            "f2" to listOf(
                DiscordMessage(
                    channelId = "dm_f2",
                    senderName = "Ayşe",
                    roleColorHex = 0xFFEB459E,
                    avatarEmoji = "🎮",
                    content = "Akşam sesli sohbet kanalına girecek misin?",
                    timestamp = "Bugün 09:40"
                )
            )
        )

        val notifications = listOf(
            DiscordNotification(
                title = "Eren_Admin senden bahsetti",
                description = "#genel-sohbet kanalında sana bir mention geldi: '@Enes hoş geldin!'",
                timestamp = "10 dk önce",
                iconEmoji = "@"
            ),
            DiscordNotification(
                title = "Yeni Sunucu Duyurusu",
                description = "Floppia Türkiye: Sezon güncellemesi ve etkinlikler yayında!",
                timestamp = "1 saat önce",
                iconEmoji = "📢"
            ),
            DiscordNotification(
                title = "Arkadaşlık İsteği Kabul Edildi",
                description = "Berk#1024 ile artık arkadaşsınız.",
                timestamp = "Dün",
                iconEmoji = "👥"
            )
        )

        return DiscordUiState(
            servers = servers,
            publicExploreServers = exploreServers,
            channels = channels,
            messagesByChannel = initialMessages,
            dmMessagesByFriend = dmMessages,
            members = members,
            friends = friends,
            notifications = notifications
        )
    }

    fun selectTab(tab: DiscordTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectServer(serverId: String) {
        val serverChannels = _uiState.value.channels.filter { it.serverId == serverId }
        val firstText = serverChannels.firstOrNull { it.type == ChannelType.TEXT }?.id ?: "c_genel"

        _uiState.update {
            it.copy(
                isDirectMessages = false,
                selectedServerId = serverId,
                selectedChannelId = firstText,
                isServerDrawerOpen = false
            )
        }
    }

    fun selectDirectMessages() {
        _uiState.update {
            it.copy(
                isDirectMessages = true,
                selectedFriendId = it.friends.firstOrNull()?.id ?: "f1",
                isServerDrawerOpen = false
            )
        }
    }

    fun selectChannel(channelId: String) {
        val channel = _uiState.value.channels.find { it.id == channelId }
        if (channel?.type == ChannelType.VOICE) {
            connectVoiceChannel(channelId)
        } else {
            _uiState.update {
                it.copy(
                    selectedChannelId = channelId,
                    isServerDrawerOpen = false
                )
            }
        }
    }

    fun selectFriend(friendId: String) {
        _uiState.update {
            it.copy(
                selectedFriendId = friendId,
                isServerDrawerOpen = false
            )
        }
    }

    fun connectVoiceChannel(channelId: String) {
        val channel = _uiState.value.channels.find { it.id == channelId } ?: return
        soundEngine.playVoiceConnected()
        _uiState.update {
            it.copy(
                connectedVoiceChannelId = channelId,
                isVoiceOverlayOpen = true,
                activeSpeakers = listOf("Eren_Admin", "Sen"),
                toastMessage = "🔊 ${channel.name} odasına bağlanıldı (RTC Bağlandı)"
            )
        }
    }

    fun disconnectVoice() {
        if (_uiState.value.connectedVoiceChannelId != null) {
            soundEngine.playVoiceDisconnected()
            _uiState.update {
                it.copy(
                    connectedVoiceChannelId = null,
                    isVoiceOverlayOpen = false,
                    activeSpeakers = emptyList(),
                    toastMessage = "📞 Ses kanalından ayrıldınız"
                )
            }
        }
    }

    fun toggleVoiceOverlay() {
        _uiState.update { it.copy(isVoiceOverlayOpen = !it.isVoiceOverlayOpen) }
    }

    fun toggleMicMute() {
        soundEngine.playMuteToggle()
        _uiState.update {
            val newMute = !it.isMicMuted
            it.copy(
                isMicMuted = newMute,
                toastMessage = if (newMute) "🔇 Mikrofon kapatıldı" else "🎙️ Mikrofon açıldı"
            )
        }
    }

    fun toggleDeafen() {
        soundEngine.playMuteToggle()
        _uiState.update {
            val newDeafen = !it.isDeafened
            it.copy(
                isDeafened = newDeafen,
                isMicMuted = if (newDeafen) true else it.isMicMuted,
                toastMessage = if (newDeafen) "🎧 Sağırlaştırıldı (Ses kesildi)" else "🎧 Kulaklık açıldı"
            )
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val trimmed = text.trim()
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        soundEngine.playMessageSent()

        if (_uiState.value.isDirectMessages || _uiState.value.currentTab == DiscordTab.MESSAGES) {
            val friendId = _uiState.value.selectedFriendId
            val userMsg = DiscordMessage(
                channelId = "dm_$friendId",
                senderName = _uiState.value.myName,
                roleColorHex = 0xFF5865F2,
                avatarEmoji = "😎",
                content = trimmed,
                timestamp = "Bugün $timeStr"
            )
            _uiState.update { state ->
                val currentList = state.dmMessagesByFriend[friendId] ?: emptyList()
                state.copy(dmMessagesByFriend = state.dmMessagesByFriend + (friendId to (currentList + userMsg)))
            }

            // Real friend chat response
            viewModelScope.launch {
                delay(1200)
                val reply = when {
                    trimmed.contains("selam", ignoreCase = true) -> "Selam dostum! Nasılsın?"
                    trimmed.contains("nasılsın", ignoreCase = true) -> "İyidir, Discord'dayım sen ne yapıyorsun?"
                    trimmed.contains("akşam", ignoreCase = true) -> "Akşam toplanırız kesinlikle!"
                    else -> "Anladım, konuşuruz birazdan!"
                }
                val friendObj = _uiState.value.friends.find { it.id == friendId }
                val replyMsg = DiscordMessage(
                    channelId = "dm_$friendId",
                    senderName = friendObj?.name ?: "Arkadaş",
                    roleColorHex = 0xFFED4245,
                    avatarEmoji = friendObj?.avatarEmoji ?: "👤",
                    content = reply,
                    timestamp = "Bugün ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}"
                )
                soundEngine.playMessageReceived()
                _uiState.update { state ->
                    val currentList = state.dmMessagesByFriend[friendId] ?: emptyList()
                    state.copy(dmMessagesByFriend = state.dmMessagesByFriend + (friendId to (currentList + replyMsg)))
                }
            }
            return
        }

        // Server Channel message (NO BOTS, pure human chat)
        val channelId = _uiState.value.selectedChannelId
        val userMsg = DiscordMessage(
            channelId = channelId,
            senderName = _uiState.value.myName,
            roleBadge = "ÜYE",
            roleColorHex = 0xFF5865F2,
            avatarEmoji = "😎",
            content = trimmed,
            timestamp = "Bugün $timeStr"
        )

        _uiState.update { state ->
            val list = state.messagesByChannel[channelId] ?: emptyList()
            state.copy(messagesByChannel = state.messagesByChannel + (channelId to (list + userMsg)))
        }
    }

    fun toggleReaction(messageId: String, emoji: String) {
        val channelId = _uiState.value.selectedChannelId
        soundEngine.vibrate(25)

        _uiState.update { state ->
            val currentList = state.messagesByChannel[channelId] ?: return@update state
            val updatedList = currentList.map { msg ->
                if (msg.id == messageId) {
                    val existing = msg.reactions.find { it.emoji == emoji }
                    val newReactions = if (existing != null) {
                        if (existing.userReacted) {
                            msg.reactions.mapNotNull {
                                if (it.emoji == emoji) {
                                    if (it.count - 1 <= 0) null else it.copy(count = it.count - 1, userReacted = false)
                                } else it
                            }
                        } else {
                            msg.reactions.map {
                                if (it.emoji == emoji) it.copy(count = it.count + 1, userReacted = true) else it
                            }
                        }
                    } else {
                        msg.reactions + DiscordReaction(emoji, 1, userReacted = true)
                    }
                    msg.copy(reactions = newReactions)
                } else msg
            }
            state.copy(messagesByChannel = state.messagesByChannel + (channelId to updatedList))
        }
    }

    fun joinExploreServer(server: DiscordServer) {
        if (_uiState.value.servers.any { it.id == server.id }) {
            selectServer(server.id)
            _uiState.update { it.copy(showExploreServersDialog = false) }
            return
        }

        val defaultChannel = DiscordChannel("c_${server.id}", server.id, "💬・genel-sohbet", ChannelType.TEXT, "Genel sohbet odası")
        _uiState.update { state ->
            state.copy(
                servers = state.servers + server,
                channels = state.channels + defaultChannel,
                selectedServerId = server.id,
                selectedChannelId = defaultChannel.id,
                isDirectMessages = false,
                showExploreServersDialog = false,
                toastMessage = "🎉 '${server.name}' sunucusuna katıldınız!"
            )
        }
    }

    fun createServer(name: String, emoji: String) {
        if (name.isBlank()) return
        val newId = "srv_${UUID.randomUUID()}"
        val newServer = DiscordServer(newId, name.trim(), if (emoji.isBlank()) "🌟" else emoji.trim(), "Yeni oluşturulan Discord topluluğu", 0, 1)
        val defaultChannel = DiscordChannel("c_${UUID.randomUUID()}", newId, "💬・genel-sohbet", ChannelType.TEXT, "Genel sohbet odası")
        val defaultVoice = DiscordChannel("v_${UUID.randomUUID()}", newId, "🔊 Genel Ses Odası", ChannelType.VOICE, "Ses kanalı")

        _uiState.update { state ->
            state.copy(
                servers = state.servers + newServer,
                channels = state.channels + listOf(defaultChannel, defaultVoice),
                selectedServerId = newId,
                selectedChannelId = defaultChannel.id,
                isDirectMessages = false,
                showCreateServerDialog = false,
                toastMessage = "🎉 '${newServer.name}' sunucusu oluşturuldu!"
            )
        }
    }

    fun createChannel(name: String, type: ChannelType) {
        if (name.isBlank()) return
        val serverId = _uiState.value.selectedServerId
        val formatted = if (type == ChannelType.TEXT) "💬・${name.trim().lowercase().replace(" ", "-")}" else "🔊 ${name.trim()}"
        val newChannel = DiscordChannel(
            id = "ch_${UUID.randomUUID()}",
            serverId = serverId,
            name = formatted,
            type = type,
            topic = "Yeni oluşturulan kanal",
            category = if (type == ChannelType.TEXT) "METİN KANALLARI" else "SES KANALLARI"
        )

        _uiState.update { state ->
            state.copy(
                channels = state.channels + newChannel,
                selectedChannelId = newChannel.id,
                showCreateChannelDialog = false,
                toastMessage = "Kanal oluşturuldu: $formatted"
            )
        }
    }

    fun updateProfile(name: String, presence: UserPresence, customStatus: String, aboutMe: String) {
        _uiState.update {
            it.copy(
                myName = if (name.isNotBlank()) name.trim() else it.myName,
                myPresence = presence,
                myCustomStatus = customStatus.trim(),
                myAboutMe = aboutMe.trim(),
                showUserSettingsDialog = false,
                toastMessage = "Profil güncellendi"
            )
        }
    }

    fun inspectMember(member: DiscordMember?) {
        _uiState.update { it.copy(inspectedMember = member) }
    }

    fun showCreateServerDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateServerDialog = show) }
    }

    fun showCreateChannelDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateChannelDialog = show) }
    }

    fun showUserSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showUserSettingsDialog = show) }
    }

    fun showExploreServersDialog(show: Boolean) {
        _uiState.update { it.copy(showExploreServersDialog = show) }
    }

    fun toggleServerDrawer() {
        _uiState.update { it.copy(isServerDrawerOpen = !it.isServerDrawerOpen) }
    }

    fun toggleMembersDrawer() {
        _uiState.update { it.copy(isMembersDrawerOpen = !it.isMembersDrawerOpen) }
    }

    fun copyText(text: String, label: String = "Kopyalandı") {
        try {
            val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            _uiState.update { it.copy(toastMessage = "📋 $label kopyalandı") }
        } catch (_: Exception) {}
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
