package gecko10000.betteranvils.di

import gecko10000.betteranvils.BetterAnvils
import gecko10000.betteranvils.CommandHandler
import gecko10000.betteranvils.managers.AnvilBlockManager
import gecko10000.betteranvils.managers.DataManager
import gecko10000.betteranvils.managers.PermissionManager
import kotlinx.serialization.json.Json
import org.koin.dsl.module

fun pluginModules(plugin: BetterAnvils) = module {
    single { plugin }
    single(createdAtStart = true) { CommandHandler() }
    single(createdAtStart = true) { DataManager() }
    single(createdAtStart = true) { AnvilBlockManager() }
    single(createdAtStart = true) { PermissionManager() }
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
}
