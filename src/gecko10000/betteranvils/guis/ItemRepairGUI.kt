package gecko10000.betteranvils.guis

import gecko10000.betteranvils.BetterAnvils
import gecko10000.betteranvils.DataManager
import gecko10000.betteranvils.di.MyKoinComponent
import gecko10000.betteranvils.model.RepairInfo
import gecko10000.geckolib.GUI
import gecko10000.geckolib.extensions.parseMM
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import redempt.redlib.inventorygui.InventoryGUI
import redempt.redlib.inventorygui.ItemButton
import redempt.redlib.itemutils.ItemUtils
import redempt.redlib.misc.Task
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ItemRepairGUI(player: Player, private val block: Block) : MyKoinComponent, GUI(player) {

    private val plugin: BetterAnvils by inject()
    private val dataManager: DataManager by inject()

    private val data = dataManager.getData(player)
    private val job: Job

    init {
        if (!Tag.ANVIL.isTagged(block.type)) {
            shouldOpen = false
        }
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                Task.syncDelayed { -> updateItemTimes() }
                delay(1.toDuration(DurationUnit.SECONDS))
            }
        }
    }

    private fun emptySlotButton(): ItemButton {
        val item = ItemStack.of(Material.YELLOW_STAINED_GLASS_PANE)
        item.editMeta {
            it.displayName(parseMM("<yellow>Empty"))
            it.lore(listOf(
                parseMM("<green>Click to set up repair")
            ))
        }
        return ItemButton.create(item) { _ ->
            RepairStartGUI(player, block)
        }
    }

    private fun updateItemTimes() {
        data.currentRepairs
        for (info: RepairInfo? in data.currentRepairs) {
            if (info == null)
        }
    }

    override fun createInventory(): InventoryGUI {
        val size = min(54, ItemUtils.minimumChestSize(data.currentRepairs.size) + 9)
        val inventory = InventoryGUI(Bukkit.createInventory(this, size, plugin.config.itemRepairName))
        inventory.fill(0, size, FILLER)

        inventory.addButton(size - 9, BACK { AnvilHomeGUI(player, block) })
        inventory.setOnDestroy { job.cancel() }
        return inventory
    }
}
