package me.arasple.mc.trchat.module.internal.data

import me.arasple.mc.trchat.api.event.CustomDatabaseEvent
import me.arasple.mc.trchat.module.conf.file.Settings
import me.arasple.mc.trchat.util.print
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.disablePlugin
import taboolib.expansion.playerDatabase
import taboolib.expansion.setupPlayerDatabase

/**
 * @author ItsFlicker
 * @since 2021/9/11 13:29
 */
@PlatformSide(Platform.BUKKIT)
object Databases {

    @Awake(LifeCycle.ENABLE)
    fun init() {
        try {
            when (val type = Settings.conf.getString("Database.Method")?.uppercase()) {
                "LOCAL", "SQLITE", null -> setupPlayerDatabase()
                "SQL", "MYSQL" -> setupPlayerDatabase(
                    Settings.conf.getConfigurationSection("Database.SQL")!!,
                    Settings.conf.getString("Database.SQL.table")!! + "_v2"
                )
                else -> {
                    val event = CustomDatabaseEvent(type)
                    event.call()
                    playerDatabase = event.database ?: error("Unsupported database type: $type")
                }
            }
        } catch (t: Throwable) {
            t.print("Failed to load database! Plugin will be disabled.")
            disablePlugin()
        }
    }

}