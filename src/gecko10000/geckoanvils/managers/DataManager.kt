package gecko10000.geckoanvils.managers

import gecko10000.geckoanvils.GeckoAnvils
import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckoanvils.model.AnvilData
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

    private val plugin: GeckoAnvils by inject()
    private val json: Json by inject()

    private val anvilData: MutableMap<UUID, AnvilData> = mutableMapOf()
    private val dataKey = NamespacedKey(plugin, "data")

    init {
        EventListener(PlayerQuitEvent::class.java) { e ->
            saveAndRemoveData(e.player)
        }
        Task.syncRepeating({ ->
            Bukkit.getOnlinePlayers().forEach(this::saveData)
        }, 0L, SAVE_INTERVAL_TICKS)
    }

    private fun loadData(player: Player): AnvilData {
        val dataString = player.persistentDataContainer.get(dataKey, PersistentDataType.STRING)
        val data = dataString?.let { json.decodeFromString(it) } ?: AnvilData(
            currentEnchants = listOf(),
            currentRepairs = listOf()
        )
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

    fun shutdown() {
        Bukkit.getOnlinePlayers().forEach(this::saveAndRemoveData)
    }

    fun getData(player: Player): AnvilData {
        return anvilData[player.uniqueId] ?: loadData(player)
    }

    fun setData(player: Player, data: AnvilData) {
        anvilData[player.uniqueId] = data
    }

    fun resetData(player: Player) {
        setData(player, AnvilData(currentEnchants = listOf(), currentRepairs = listOf()))
    }

}
