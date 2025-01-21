package gecko10000.betteranvils.guis

import gecko10000.betteranvils.BetterAnvils
import gecko10000.betteranvils.DurationFormatter
import gecko10000.betteranvils.di.MyKoinComponent
import gecko10000.geckolib.extensions.MM
import gecko10000.geckolib.extensions.parseMM
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import redempt.redlib.inventorygui.InventoryGUI
import kotlin.math.max
import kotlin.time.Duration

class RepairStartGUI(player: Player, block: Block) : MyKoinComponent, AnvilAssociatedGUI(player, block) {

    companion object {
        private const val SIZE = 27
        private const val DAMAGED_ITEM_SLOT = 10
        private const val REPAIR_ITEM_SLOT = 12
        private const val TIME_SLOT = 14
        private const val RESULT_SLOT = 16
    }

    private val plugin: BetterAnvils by inject()

    private var itemToRepair: ItemStack?
        get() = inventory.inventory.getItem(DAMAGED_ITEM_SLOT)
        set(value) = inventory.inventory.setItem(DAMAGED_ITEM_SLOT, value)
    private var sacrificedItem: ItemStack?
        get() = inventory.inventory.getItem(REPAIR_ITEM_SLOT)
        set(value) = inventory.inventory.setItem(REPAIR_ITEM_SLOT, value)
    private var duration: Duration? = null

    private fun timeItem(duration: Duration): ItemStack {
        val item = ItemStack.of(Material.CLOCK, max(1, duration.inWholeHours.toInt()))
        item.editMeta {
            it.displayName(parseMM("<gold>Repair will take"))
            it.lore(
                listOf(
                    MM.deserialize(
                        "<!i><yellow><time>",
                        Placeholder.unparsed("time", DurationFormatter.format(duration))
                    )
                )
            )
        }
        return item
    }

    private fun updateInventory(gui: InventoryGUI = this.inventory) {
        gui.inventory.setItem(TIME_SLOT, duration?.let { timeItem(it) } ?: FILLER)
        gui.inventory.setItem(RESULT_SLOT, null)
    }

    override fun createInventory(): InventoryGUI {
        val inventory = InventoryGUI(Bukkit.createInventory(this, SIZE, plugin.config.repairStartName))
        inventory.fill(0, SIZE, FILLER)
        for (slot in setOf(DAMAGED_ITEM_SLOT, REPAIR_ITEM_SLOT)) {
            inventory.inventory.setItem(slot, null)
            inventory.openSlot(slot)
        }
        inventory.addButton(SIZE - 9, BACK { ItemRepairGUI(player, block) })
        updateInventory(inventory)
        return inventory
    }
}
