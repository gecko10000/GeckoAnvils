package gecko10000.betteranvils

import gecko10000.betteranvils.config.Config
import gecko10000.betteranvils.di.MyKoinComponent
import gecko10000.betteranvils.di.MyKoinContext
import gecko10000.betteranvils.managers.DataManager
import gecko10000.geckolib.config.YamlFileManager
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.inject

class BetterAnvils : JavaPlugin(), MyKoinComponent {

    private val dataManager: DataManager by inject()

    private val configFile = YamlFileManager(
        configDirectory = dataFolder,
        initialValue = Config(),
        serializer = Config.serializer()
    )
    val config: Config
        get() = configFile.value

    override fun onEnable() {
        MyKoinContext.init(this)
    }

    override fun onDisable() {
        dataManager.shutdown()
    }

    fun reloadConfigs() {
        configFile.reload()
    }

}
