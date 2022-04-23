package me.arasple.mc.trchat.module.display.channel

import me.arasple.mc.trchat.api.event.TrChatEvent
import me.arasple.mc.trchat.module.display.channel.obj.ChannelBindings
import me.arasple.mc.trchat.module.display.channel.obj.ChannelSettings
import me.arasple.mc.trchat.module.display.channel.obj.Target
import me.arasple.mc.trchat.module.display.filter.ChatFilter
import me.arasple.mc.trchat.module.display.format.Format
import me.arasple.mc.trchat.module.internal.data.ChatLogs
import me.arasple.mc.trchat.module.internal.service.Metrics
import me.arasple.mc.trchat.util.*
import me.arasple.mc.trchat.util.proxy.Proxy
import me.arasple.mc.trchat.util.proxy.sendBukkitMessage
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.command
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.unregisterCommand
import taboolib.common.util.subList
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import taboolib.platform.util.toProxyLocation
import java.util.*

/**
 * @author wlys
 * @since 2021/12/11 22:27
 */
open class Channel(
    val id: String,
    val settings: ChannelSettings,
    val bindings: ChannelBindings,
    val formats: List<Format>,
    val console: Format? = null
) {

    init {
        if (!bindings.command.isNullOrEmpty()) {
            command(bindings.command[0], subList(bindings.command, 1), "Channel $id speak command", permission = settings.joinPermission ?: "") {
                execute<Player> { sender, _, _ ->
                    if (sender.getSession().channel == this@Channel) {
                        quit(sender)
                    } else {
                        join(sender, this@Channel)
                    }
                }
                dynamic("message", optional = true) {
                    execute<CommandSender> { sender, _, argument ->
                        if (sender is Player) {
                            execute(sender, argument)
                        } else {
//                            val builder = Component.text()
//                            console?.let { format ->
//                                format.prefix.forEach { prefix ->
//                                    builder.append(prefix.value.first().content.toTextComponent(player)) }
//                                builder.append(format.msg.serialize(player, argument, settings.disabledFunctions))
//                                format.suffix.forEach { suffix ->
//                                    builder.append(suffix.value.first().content.toTextComponent(player)) }
//                            } ?: return@execute
//                            val component = builder.build()
//
//                            if (settings.proxy && Proxy.isEnabled) {
//                                val gson = gson(component)
//                                if (settings.ports != null) {
//                                    player.sendBukkitMessage(
//                                        "ForwardRaw",
//                                        player.uniqueId.toString(),
//                                        gson,
//                                        settings.joinPermission ?: "null",
//                                        settings.ports.joinToString(";")
//                                    )
//                                } else {
//                                    player.sendBukkitMessage(
//                                        "BroadcastRaw",
//                                        player.uniqueId.toString(),
//                                        gson,
//                                        settings.joinPermission ?: "null"
//                                    )
//                                }
//                                return
//                            }
                        }
                    }
                }
                incorrectSender { sender, _ ->
                    sender.sendLang("Command-Not-Player")
                }
            }
        }
    }

    val listeners = mutableSetOf<UUID>()

    open fun execute(player: Player, message: String) {
        if (!player.checkMute()) {
            return
        }
        if (!settings.speakCondition.pass(player)) {
            player.sendLang("Channel-No-Speak-Permission")
            return
        }
        if (settings.filterBeforeSending && ChatFilter.filter(message).sensitiveWords > 0) {
            player.sendLang("Channel-Filter-Before-Sending")
            return
        }
        val event = TrChatEvent(this, player.getSession(), message)
        if (!event.call()) {
            return
        }
        val msg = event.message

        val builder = Component.text()
        formats.firstOrNull { it.condition.pass(player) }?.let { format ->
            format.prefix.forEach { prefix ->
                builder.append(prefix.value.first { it.condition.pass(player) }.content.toTextComponent(player)) }
            builder.append(format.msg.serialize(player, msg, settings.disabledFunctions))
            format.suffix.forEach { suffix ->
                builder.append(suffix.value.first { it.condition.pass(player) }.content.toTextComponent(player)) }
        } ?: return
        val component = builder.build()

        if (settings.proxy && Proxy.isEnabled) {
            val gson = gson(component)
            if (settings.ports != null) {
                player.sendBukkitMessage(
                    "ForwardRaw",
                    player.uniqueId.toString(),
                    gson,
                    settings.joinPermission ?: "null",
                    settings.ports.joinToString(";")
                )
            } else {
                player.sendBukkitMessage(
                    "BroadcastRaw",
                    player.uniqueId.toString(),
                    gson,
                    settings.joinPermission ?: "null"
                )
            }
            return
        }
        when (settings.target.range) {
            Target.Range.ALL -> {
                listeners.forEach {
                    getProxyPlayer(it)?.sendProcessedMessage(player, component)
                }
            }
            Target.Range.SINGLE_WORLD -> {
                onlinePlayers().filter { listeners.contains(it.uniqueId) && it.world == player.world.name }.forEach {
                    it.sendProcessedMessage(player, component)
                }
            }
            Target.Range.DISTANCE -> {
                onlinePlayers().filter { listeners.contains(it.uniqueId)
                        && it.world == player.world.name
                        && it.location.distance(player.location.toProxyLocation()) <= settings.target.distance }.forEach {
                    it.sendProcessedMessage(player, component)
                }
            }
            Target.Range.SELF -> {
                player.sendProcessedMessage(player, component)
            }
        }
        console().cast<CommandSender>().sendProcessedMessage(player, component)

        player.getSession().lastMessage = message
        ChatLogs.log(player, message)
        Metrics.increase(0)
    }

    open fun unregister() {
        bindings.command?.forEach { unregisterCommand(it) }
        listeners.clear()
    }

    companion object {

        val channels = mutableListOf<Channel>()

        var defaultChannel: Channel? = null

        fun join(player: Player, channel: String, hint: Boolean = true) {
            channels.firstOrNull { it.id == channel }?.let {
                join(player, it, hint)
            } ?: quit(player)
        }

        fun join(player: Player, channel: Channel, hint: Boolean = true) {
            if (channel.settings.joinPermission?.let { player.hasPermission(it) } == false) {
                player.sendLang("General-No-Permission")
                return
            }
            player.getSession().channel = channel
            channel.listeners.add(player.uniqueId)

            if (hint) {
                player.sendLang("Channel-Join", channel.id)
            }
        }

        fun quit(player: Player) {
            player.getSession().channel?.let {
                if (!it.settings.autoJoin) {
                    it.listeners.remove(player.uniqueId)
                }
                player.sendLang("Channel-Quit", it.id)
            }
            player.getSession().channel = defaultChannel
        }
    }
}