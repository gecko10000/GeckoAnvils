package gecko10000.betteranvils.di

import gecko10000.betteranvils.BetterAnvils
import gecko10000.betteranvils.DataManager
import gecko10000.betteranvils.InteractionManager
import kotlinx.serialization.json.Json
import org.koin.dsl.module

fun pluginModules(plugin: BetterAnvils) = module {
    single { plugin }
    single(createdAtStart = true) { DataManager() }
    single(createdAtStart = true) { InteractionManager() }
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
}
