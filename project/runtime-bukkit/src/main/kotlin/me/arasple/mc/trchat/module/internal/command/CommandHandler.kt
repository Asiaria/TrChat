package me.arasple.mc.trchat.module.internal.command

import me.arasple.mc.trchat.module.internal.TrChatBukkit
import me.arasple.mc.trchat.module.internal.command.sub.CommandColor
import me.arasple.mc.trchat.module.internal.command.sub.CommandRecallMessage
import me.arasple.mc.trchat.util.data
import me.arasple.mc.trchat.util.parseSimple
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * CommandHandler
 * me.arasple.mc.trchat.module.internal.command
 *
 * @author ItsFlicker
 * @since 2021/8/21 12:23
 */
@PlatformSide(Platform.BUKKIT)
@CommandHeader("trchat", ["trc"], "TrChat main command", permission = "trchat.access")
object CommandHandler {

    @CommandBody(permission = "trchat.command.color", optional = true)
    val color = CommandColor.command

    @CommandBody(permission = "trchat.command.recallmessage", optional = true)
    val recallMessage = CommandRecallMessage.command

    @CommandBody(permission = "trchat.command.chatfilter", optional = true)
    val chatFilter = subCommand {
        dynamic("option") {
            suggest { listOf("on", "off") }
            execute<Player> { sender, _, arg ->
                if (arg == "on") {
                    sender.data.setFilter(true)
                    sender.sendMessage("§7你已经开启聊天过滤器")
                } else {
                    sender.data.setFilter(false)
                    sender.sendMessage("§7你已经关闭聊天过滤器")
                }
            }
        }
    }

    @CommandBody(permission = "trchat.command.clear", optional = true)
    val clear = subCommand {
        player(suggest = listOf("*")) {
            execute<CommandSender> { _, ctx, _ ->
                ctx.players("player").forEach {
                    repeat(80) { _ ->
                        it.sendMessage("")
                    }
                }
            }
        }
    }

    @CommandBody(permission = "trchat.command.tellsimple", optional = true)
    val tellsimple = subCommand {
        player(suggest = listOf("*")) {
            dynamic("message") {
                execute<CommandSender> { _, ctx, argument ->
                    val component = argument.parseSimple()
                    ctx.players("player").forEach {
                        component.sendTo(it)
                    }
                }
            }
        }
    }

    @CommandBody(permission = "trchat.command.spy", optional = true)
    val spy = subCommand {
        execute<Player> { sender, _, _ ->
            val state = sender.data.switchSpy()
            sender.sendLang(if (state) "Private-Message-Spy-On" else "Private-Message-Spy-Off")
        }
    }

    @CommandBody(permission = "trchat.command.vanish", optional = true)
    val vanish = subCommand {
        execute<Player> { sender, _, _ ->
            sender.data.switchVanish()
        }
    }

    @CommandBody(permission = "trchat.command.reload", optional = true)
    val reload = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            TrChatBukkit.reload(sender)
        }
    }

    @CommandBody
    val help = subCommand {
        createHelper()
    }

    @CommandBody
    val main = mainCommand {
        createHelper()
        incorrectSender { sender, _ ->
            sender.sendLang("Command-Not-Player")
        }
        incorrectCommand { _, _, _, _ ->
            createHelper()
        }
    }

}