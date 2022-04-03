package me.arasple.mc.trchat.module.display.format.part.json

import me.arasple.mc.trchat.module.internal.script.Condition
import me.arasple.mc.trchat.util.color.colorify
import me.arasple.mc.trchat.util.legacy
import me.arasple.mc.trchat.util.pass
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.entity.Player
import taboolib.common.util.replaceWithOrder
import taboolib.platform.compat.replacePlaceholder

/**
 * @author wlys
 * @since 2022/1/22 9:45
 */
class Hover(val content: Map<String, Condition?>) {

    fun process(builder: TextComponent.Builder, player: Player, vararg vars: String, message: String = "") {
        val text = content.entries.filter { it.value.pass(player) }.joinToString("\n") { it.key }
            .replacePlaceholder(player).replace("\$message", message).replaceWithOrder(*vars).colorify()

        builder.hoverEvent(HoverEvent.showText(legacy(text)))
    }
}