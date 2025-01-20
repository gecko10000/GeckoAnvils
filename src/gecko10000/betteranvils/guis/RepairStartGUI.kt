package gecko10000.betteranvils.guis

import gecko10000.betteranvils.BetterAnvils
import gecko10000.betteranvils.di.MyKoinComponent
import gecko10000.geckolib.GUI
import gecko10000.geckolib.extensions.MM
import gecko10000.geckolib.extensions.parseMM
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import redempt.redlib.inventorygui.InventoryGUI
import kotlin.math.max
import kotlin.time.Duration

class RepairStartGUI(player: Player, private val block: Block) : MyKoinComponent, GUI(player) {

    companion object {
        private const val SIZE = 27
        private const val DAMAGED_ITEM_SLOT = 10
        private const val REPAIR_ITEM_SLOT = 12
        private const val TIME_SLOT = 14
        private const val RESULT_SLOT = 16
    }

    private val plugin: BetterAnvils by inject()

    private var duration: Duration? = null

    init {
        if (!Tag.ANVIL.isTagged(block.type)) {
            shouldOpen = false
        }
    }

    private fun durationToString(duration: Duration): String {
        val durationString = StringBuilder()
        val days = duration.inWholeDays
        if (days > 0) durationString.append("${days}d ")
        val hours = duration.inWholeHours % 24
        if (hours > 0) durationString.append("${hours}h ")
        val minutes = duration.inWholeMinutes % 60
        if (minutes > 0) durationString.append("${minutes}m ")
        val seconds = duration.inWholeSeconds % 60
        if (seconds > 0) durationString.append("${seconds}s ")
        return durationString.toString().trimEnd()
    }

    private fun timeItem(duration: Duration): ItemStack {
        val item = ItemStack.of(Material.CLOCK, max(1, duration.inWholeHours.toInt()))
        item.editMeta {
            it.displayName(parseMM("<gold>Repair will take"))
            it.lore(
                listOf(
                    MM.deserialize("<!i><yellow><time>", Placeholder.unparsed("time", durationToString(duration)))
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
