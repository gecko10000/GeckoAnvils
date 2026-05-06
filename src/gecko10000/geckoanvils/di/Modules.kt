package gecko10000.geckoanvils.di

import gecko10000.geckoanvils.GeckoAnvils
import gecko10000.geckoanvils.managers.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

fun pluginModules(plugin: GeckoAnvils) = module {
    single { plugin }
    single(createdAtStart = true) { DataManager() }
    single(createdAtStart = true) { AnvilBlockManager() }
    single(createdAtStart = true) { PermissionManager() }
    single(createdAtStart = true) { EnchantCombineManager() }
    single(createdAtStart = true) { RepairCombineManager() }
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
}
