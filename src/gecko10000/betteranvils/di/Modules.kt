package gecko10000.betteranvils.di

import gecko10000.betteranvils.BetterAnvils
import kotlinx.serialization.json.Json
import org.koin.dsl.module

fun pluginModules(plugin: BetterAnvils) = module {
    single { plugin }
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
}
