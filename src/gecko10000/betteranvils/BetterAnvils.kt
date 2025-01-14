package gecko10000.betteranvils

import gecko10000.betteranvils.config.Config
import gecko10000.betteranvils.di.MyKoinContext
import gecko10000.geckolib.config.YamlFileManager
import org.bukkit.plugin.java.JavaPlugin

class BetterAnvils : JavaPlugin() {

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

    fun reloadConfigs() {
        configFile.reload()
    }

}
