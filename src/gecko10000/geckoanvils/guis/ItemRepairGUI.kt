package gecko10000.geckoanvils.guis

import gecko10000.geckoanvils.DurationUtils
import gecko10000.geckoanvils.DurationUtils.addProgressInfo
import gecko10000.geckoanvils.GeckoAnvils
import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckoanvils.managers.DataManager
import gecko10000.geckoanvils.managers.PermissionManager
import gecko10000.geckoanvils.model.RepairInfo
import gecko10000.geckolib.extensions.extend
import gecko10000.geckolib.extensions.parseMM
import gecko10000.geckolib.extensions.updated
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import redempt.redlib.inventorygui.InventoryGUI
import redempt.redlib.inventorygui.ItemButton
import redempt.redlib.itemutils.ItemUtils
import redempt.redlib.misc.Task
import kotlin.math.min

class ItemRepairGUI(player: Player, block: Block) : MyKoinComponent, AnvilAssociatedGUI(player, block) {

    private val plugin: GeckoAnvils by inject()
    private val dataManager: DataManager by inject()
    private val permissionManager: PermissionManager by inject()

    private val data = dataManager.getData(player)
    private val concurrentRepairs =
        data.currentRepairs.extend(targetAmount = permissionManager.getAllowedRepairs(player), element = null)
    private val job: Job

    init {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                Task.syncDelayed { -> updateInventory() }
                delay(DurationUtils.ONE_SEC)
            }
        }
    }

    private fun emptySlotButton(): ItemButton {
        val item = ItemStack.of(Material.LIME_STAINED_GLASS_PANE)
        item.editMeta {
            it.displayName(parseMM("<gray>(<green>+</green>) <dark_green>Start Repair"))
        }
        return ItemButton.create(item) { _ ->
            RepairStartGUI(player, block)
        }
    }

    private fun inRepairItem(index: Int, data: RepairInfo): ItemButton {
        val passedTime = System.currentTimeMillis() - data.startTime
        val progress = passedTime.toDouble() / data.duration
        val remainingTime = data.duration - passedTime
        if (remainingTime <= 0) return finishedRepairButton(index, data)
        val displayItem = data.outputItem.clone()
        displayItem.addProgressInfo(progress, remainingTime)
        return ItemButton.create(displayItem) { _ -> }
    }

    private fun finishedRepairButton(index: Int, data: RepairInfo): ItemButton {
        val displayItem = data.outputItem.clone()
        displayItem.editMeta {
            it.lore(
                it.lore().orEmpty().plus(
                    listOf(
                        Component.empty(),
                        parseMM("<green>Done! Click to collect.")
                    )
                )
            )
        }
        return ItemButton.create(displayItem) { _ ->
            val item = data.outputItem
            val newRepairs = concurrentRepairs.updated(index) { null }.dropLastWhile { it == null }
            val newData = this.data.copy(currentRepairs = newRepairs)
            dataManager.setData(player, newData)
            ItemUtils.give(player, item)
            EnchantGUI(player, block)
        }
    }

    private fun updateInventory(gui: InventoryGUI = this.inventory) {
        for (i in concurrentRepairs.indices) {
            val info = concurrentRepairs[i]
            val button = if (info == null) emptySlotButton() else inRepairItem(i, info)
            gui.addButton(i, button)
        }
    }

    override fun createInventory(): InventoryGUI {
        val size = min(54, ItemUtils.minimumChestSize(data.currentRepairs.size) + 9)
        val inventory = InventoryGUI(Bukkit.createInventory(this, size, plugin.config.itemRepairName))
        inventory.fill(0, size, FILLER)

        inventory.addButton(size - 9, BACK { AnvilHomeGUI(player, block) })
        inventory.setOnDestroy { job.cancel() }
        updateInventory(inventory)
        return inventory
    }
}
