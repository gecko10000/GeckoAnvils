package gecko10000.betteranvils.guis

import gecko10000.betteranvils.BetterAnvils
import gecko10000.betteranvils.di.MyKoinComponent
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.koin.core.component.inject
import redempt.redlib.inventorygui.InventoryGUI

class EnchantGUI(player: Player, block: Block) : MyKoinComponent, AnvilAssociatedGUI(player, block) {

    companion object {
        private const val SIZE = 45
        private const val OUTPUT_SLOT = 31
    }

    private val plugin: BetterAnvils by inject()

    override fun createInventory(): InventoryGUI {
        val inventory = InventoryGUI(Bukkit.createInventory(this, SIZE, plugin.config.enchantmentAnvilName))
        inventory.fill(0, 9, FILLER)
        inventory.fill(18, SIZE, FILLER)
        inventory.inventory.setItem(OUTPUT_SLOT, null)
        inventory.openSlots(9, 18)
        inventory.openSlot(OUTPUT_SLOT)
        inventory.addButton(SIZE - 9, BACK { AnvilHomeGUI(player, block) })
        return inventory
    }
}
