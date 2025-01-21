package gecko10000.betteranvils.guis

import gecko10000.betteranvils.BetterAnvils
import gecko10000.betteranvils.di.MyKoinComponent
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.koin.core.component.inject
import redempt.redlib.inventorygui.InventoryGUI

class EnchantStartGUI(player: Player, block: Block) : MyKoinComponent, AnvilAssociatedGUI(player, block) {

    companion object {
        private const val SIZE = 54
    }

    private val plugin: BetterAnvils by inject()

    override fun createInventory(): InventoryGUI {
        val inventory = InventoryGUI(Bukkit.createInventory(this, SIZE, plugin.config.enchantmentStartName))
        inventory.fill(0, SIZE, FILLER)
        inventory.addButton(SIZE - 9, BACK { EnchantGUI(player, block) })
        return inventory
    }
}
