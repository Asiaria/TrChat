package me.arasple.mc.trchat.api.config

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common5.util.parseMillis
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer
import taboolib.module.configuration.Configuration

/**
 * @author wlys
 * @since 2021/12/11 23:59
 */
@PlatformSide([Platform.BUKKIT])
object Settings {

    @Config("settings.yml", autoReload = true)
    lateinit var CONF: Configuration
        private set

    @ConfigNode("Chat.Cooldown", "settings.yml")
    val chatCooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    @ConfigNode("Chat.Anti-Repeat", "settings.yml")
    var chatSimilarity = 0.85

    @ConfigNode("Chat.Length-Limit", "settings.yml")
    var chatLengthLimit = 100
}