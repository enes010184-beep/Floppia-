package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FloppiaUltimate", appName)
    }

    @Test
    fun `test discord bottom navigation tabs`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = FloppiaViewModel(app)

        assertEquals(DiscordTab.SERVERS, vm.uiState.value.currentTab)

        vm.selectTab(DiscordTab.MESSAGES)
        assertEquals(DiscordTab.MESSAGES, vm.uiState.value.currentTab)

        vm.selectTab(DiscordTab.NOTIFICATIONS)
        assertEquals(DiscordTab.NOTIFICATIONS, vm.uiState.value.currentTab)

        vm.selectTab(DiscordTab.YOU)
        assertEquals(DiscordTab.YOU, vm.uiState.value.currentTab)
    }

    @Test
    fun `test message sending and reaction`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = FloppiaViewModel(app)

        val initialMessages = vm.uiState.value.messagesByChannel["c_genel"]?.size ?: 0
        vm.sendMessage("Gerçek Discord mesajı!")

        val updated = vm.uiState.value.messagesByChannel["c_genel"] ?: emptyList()
        assertEquals(initialMessages + 1, updated.size)
        assertEquals("Gerçek Discord mesajı!", updated.last().content)

        // Test reaction
        val msgId = updated.last().id
        vm.toggleReaction(msgId, "🔥")
        val withReaction = vm.uiState.value.messagesByChannel["c_genel"]?.find { it.id == msgId }
        assertNotNull(withReaction)
        assertTrue(withReaction?.reactions?.any { it.emoji == "🔥" } == true)
    }

    @Test
    fun `test voice call connection and overlay`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = FloppiaViewModel(app)

        vm.connectVoiceChannel("v_genel")
        assertEquals("v_genel", vm.uiState.value.connectedVoiceChannelId)
        assertTrue(vm.uiState.value.isVoiceOverlayOpen)

        vm.toggleMicMute()
        assertTrue(vm.uiState.value.isMicMuted)

        vm.disconnectVoice()
        assertEquals(null, vm.uiState.value.connectedVoiceChannelId)
    }

    @Test
    fun `test exploring and joining public servers`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = FloppiaViewModel(app)

        val exploreServer = vm.uiState.value.publicExploreServers.first()
        val initialCount = vm.uiState.value.servers.size

        vm.joinExploreServer(exploreServer)
        assertEquals(initialCount + 1, vm.uiState.value.servers.size)
        assertEquals(exploreServer.id, vm.uiState.value.selectedServerId)
    }

    @Test
    fun `test profile update and presence`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = FloppiaViewModel(app)

        vm.updateProfile("Enes_Gamer", UserPresence.DND, "Oyun oynuyor", "Bio açıklaması")
        assertEquals("Enes_Gamer", vm.uiState.value.myName)
        assertEquals(UserPresence.DND, vm.uiState.value.myPresence)
        assertEquals("Oyun oynuyor", vm.uiState.value.myCustomStatus)
    }
}
