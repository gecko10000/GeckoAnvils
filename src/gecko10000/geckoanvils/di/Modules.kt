package gecko10000.geckoanvils.di

import gecko10000.geckoanvils.CommandHandler
import gecko10000.geckoanvils.GeckoAnvils
import gecko10000.geckoanvils.managers.AnvilBlockManager
import gecko10000.geckoanvils.managers.DataManager
import gecko10000.geckoanvils.managers.PermissionManager
import kotlinx.serialization.json.Json
import org.koin.dsl.module

fun pluginModules(plugin: GeckoAnvils) = module {
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
