package me.arasple.mc.trchat.module.internal

import me.arasple.mc.trchat.api.impl.BungeeChannelManager
import me.arasple.mc.trchat.api.impl.BungeeProxyManager
import net.md_5.bungee.api.ProxyServer
import taboolib.common.platform.*
import taboolib.common.platform.command.command
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.common.platform.function.server
import taboolib.module.lang.sendLang

/**
 * @author Arasple
 * @date 2019/8/4 22:42
 */
@PlatformSide([Platform.BUNGEE])
object TrChatBungee : Plugin() {

    const val TRCHAT_CHANNEL = "trchat:main"

    override fun onLoad() {
        console().sendLang("Plugin-Loading", server<ProxyServer>().version)
        console().sendLang("Plugin-Proxy-Supported", "Bungee")
    }

    override fun onEnable() {
        command("muteallservers", permission = "trchatb.muteallservers") {
            dynamic("state") {
                suggestion<ProxyCommandSender> { _, _ ->
                    listOf("on", "off")
                }
                execute<ProxyCommandSender> { _, _, argument ->
                    server<ProxyServer>().servers.forEach { (_, v) ->
                        BungeeProxyManager.sendTrChatMessage(v, "GlobalMute", argument)
                    }
                }
            }
        }
        BungeeChannelManager.loadChannels(console())
        console().sendLang("Plugin-Enabled", pluginVersion)
    }
}
