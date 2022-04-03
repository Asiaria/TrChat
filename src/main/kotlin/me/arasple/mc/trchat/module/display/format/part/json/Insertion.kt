package me.arasple.mc.trchat.module.display.format.part.json

import me.arasple.mc.trchat.module.display.format.part.Part
import me.arasple.mc.trchat.module.internal.script.Condition
import me.arasple.mc.trchat.util.Regexs
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import taboolib.common.util.replaceWithOrder
import taboolib.platform.compat.replacePlaceholder

/**
 * @author wlys
 * @since 2022/1/21 23:21
 */
class Insertion(override val content: String, override val condition: Condition?) : Part() {

    override val dynamic by lazy { Regexs.containsPlaceholder(content) }

    override fun process(builder: TextComponent.Builder, player: Player, vararg vars: String, message: String) {
        if (dynamic) {
            builder.insertion(content.replacePlaceholder(player).replace("\$message", message).replaceWithOrder(*vars))
        } else {
            builder.insertion(content.replace("\$message", message).replaceWithOrder(*vars))
        }
    }
}