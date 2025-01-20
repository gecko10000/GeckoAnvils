package gecko10000.betteranvils

import gecko10000.betteranvils.di.MyKoinComponent
import gecko10000.betteranvils.model.PlayerRepairs
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.inject
import redempt.redlib.misc.EventListener
import redempt.redlib.misc.Task
import java.util.*

class DataManager : MyKoinComponent {

    companion object {
        private const val SAVE_INTERVAL_TICKS: Long = 20 * 60 * 5
    }

    private val plugin: BetterAnvils by inject()
    private val json: Json by inject()

    private val anvilData: MutableMap<UUID, PlayerRepairs> = mutableMapOf()
    private val dataKey = NamespacedKey(plugin, "data")

    init {
        EventListener(PlayerQuitEvent::class.java) { e ->
            saveAndRemoveData(e.player)
        }
        Task.syncRepeating({ ->
            Bukkit.getOnlinePlayers().forEach(this::saveData)
        }, 0L, SAVE_INTERVAL_TICKS)
    }

    private fun loadData(player: Player): PlayerRepairs {
        val dataString = player.persistentDataContainer.get(dataKey, PersistentDataType.STRING)
        val data = dataString?.let { json.decodeFromString(it) } ?: PlayerRepairs(currentRepairs = listOf())
        anvilData[player.uniqueId] = data
        return data
    }

    private fun saveData(player: Player) {
        val data = anvilData[player.uniqueId] ?: return
        val dataString = json.encodeToString(data)
        player.persistentDataContainer.set(dataKey, PersistentDataType.STRING, dataString)
    }

    private fun saveAndRemoveData(player: Player) {
        saveData(player)
        anvilData.remove(player.uniqueId)
    }

    fun getData(player: Player): PlayerRepairs {
        return anvilData[player.uniqueId] ?: loadData(player)
    }

    fun setData(player: Player, data: PlayerRepairs) {
        anvilData[player.uniqueId] = data
    }

}
