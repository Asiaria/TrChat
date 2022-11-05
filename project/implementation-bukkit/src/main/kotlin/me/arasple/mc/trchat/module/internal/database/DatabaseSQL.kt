package me.arasple.mc.trchat.module.internal.database

import me.arasple.mc.trchat.module.conf.file.Settings
import org.bukkit.OfflinePlayer
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.module.database.ColumnOptionSQL
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.Table
import taboolib.module.database.getHost
import java.util.concurrent.ConcurrentHashMap

class DatabaseSQL : Database() {

    val host = Settings.CONF.getHost("Database.SQL")

    val table = Table(Settings.CONF.getString("Database.SQL.table")!!, host) {
        add {
            name("user")
            type(ColumnTypeSQL.VARCHAR, 36) {
                options(ColumnOptionSQL.PRIMARY_KEY)
            }
        }
        add {
            name("data")
            type(ColumnTypeSQL.MEDIUMTEXT)
        }
    }

    val dataSource = host.createDataSource()
    val cache = ConcurrentHashMap<String, Configuration>()

    init {
        table.workspace(dataSource) { createTable(true) }.run()
    }

    override fun pull(player: OfflinePlayer): ConfigurationSection {
        return cache.computeIfAbsent(player.name!!) {
            table.workspace(dataSource) {
                select { where { "user" eq player.name!! } }
            }.firstOrNull {
                Configuration.loadFromString(getString("data"))
            } ?: Configuration.empty(Type.YAML)
        }
    }

    override fun push(player: OfflinePlayer) {
        val file = cache[player.name] ?: return
        if (table.workspace(dataSource) { select { where { "user" eq player.name!! } } }.find()) {
            table.workspace(dataSource) {
                update {
                    set("data", file.saveToString())
                    where {
                        "user" eq player.name!!
                    }
                }
            }.run()
        } else {
            table.workspace(dataSource) {
                insert("user", "data") {
                    value(player.name!!, file.saveToString())
                }
            }.run()
        }
    }

    override fun release(player: OfflinePlayer) {
        cache.remove(player.name)
    }
}