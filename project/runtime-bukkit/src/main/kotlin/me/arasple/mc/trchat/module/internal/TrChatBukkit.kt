package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.api.impl.BukkitProxyManager
import me.arasple.mc.trchat.module.conf.file.Filters
import me.arasple.mc.trchat.module.conf.file.Functions
import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.module.internal.data.PlayerData
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.*
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.module.lang.sendLang
import taboolib.module.nms.MinecraftVersion.majorLegacy
import taboolib.module.nms.disablePacketListener
import taboolib.platform.Folia

@PlatformSide(Platform.BUKKIT)
object TrChatBukkit : Plugin() {

    var isPaperEnv = false
        private set

    var isGlobalMuting = false

    @Awake(LifeCycle.CONST)
    internal fun detectPaperEnv() {
        try {
            // Paper 1.16.5+
            Class.forName("com.destroystokyo.paper.PaperConfig")
            if (majorLegacy >= 11604) {
                isPaperEnv = true
            }
        } catch (_: ClassNotFoundException) {
        }
    }

    override fun onLoad() {
        console().sendLang("Plugin-Loading", Bukkit.getBukkitVersion())
    }

    override fun onEnable() {
        if (Folia.isFolia) {
            disablePacketListener()
        }
        BukkitProxyManager.processor
        HookPlugin.printInfo()
        reload(console())
        console().sendLang("Plugin-Enabled", pluginVersion)
    }

    override fun onDisable() {
        BukkitProxyManager.close()

        ChatSession.sessions.clear()
        PlayerData.data.clear()
        Channel.channels.clear()
        Function.functions.clear()
    }

    fun reload(notify: ProxyCommandSender) {
        Settings.conf.reload()
        Functions.conf.reload()
        Filters.conf.reload()
        TrChat.api().getChannelManager().loadChannels(notify)
    }

}