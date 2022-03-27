package me.arasple.mc.trchat.module.internal.command.main

import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.display.channel.PrivateChannel
import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.checkMute
import me.arasple.mc.trchat.util.getSession
import me.arasple.mc.trchat.util.proxy.bukkit.Players
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * CommandReply
 * me.arasple.mc.trchat.module.internal.command
 *
 * @author wlys
 * @since 2021/7/21 11:14
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object CommandReply {

    val lastMessageFrom = HashMap<String, String>()

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("reply", listOf("r"), "Reply", permission = "trchat.private") {
            dynamic("message") {
                execute<Player> { sender, _, argument ->
                    val session = sender.getSession()
                    if (sender.checkMute()) {
                        if (lastMessageFrom.containsKey(sender.name)) {
                            val to = Players.getPlayerFullName(lastMessageFrom[sender.name]!!)
                                ?: return@execute sender.sendLang("Command-Player-Not-Exist")
                            session.lastPrivateTo = to
                            Channel.channels.firstOrNull { it is PrivateChannel
                                    && (it.settings.joinPermission == null || sender.hasPermission(it.settings.joinPermission)) }
                                ?.execute(sender, argument)
                        }
                    }
                }
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
            incorrectCommand { sender, _, _, _ ->
                sender.sendLang("Private-Message-No-Message")
            }
        }
    }
}