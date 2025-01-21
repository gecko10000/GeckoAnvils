package gecko10000.betteranvils.di

import gecko10000.betteranvils.*
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
