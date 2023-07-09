package me.arasple.mc.trchat.api.event

import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.display.channel.Channel
import taboolib.platform.type.BukkitProxyEvent

/**
 * TrChatEvent
 * me.arasple.mc.trchat.api.event
 *
 * @author ItsFlicker
 * @since 2021/8/20 20:53
 */
class TrChatEvent(
    val channel: Channel,
    val session: ChatSession,
    var message: String,
    val forward: Boolean = true
) : BukkitProxyEvent()