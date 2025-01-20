package gecko10000.betteranvils.guis

import gecko10000.betteranvils.BetterAnvils
import gecko10000.betteranvils.di.MyKoinComponent
import gecko10000.geckolib.GUI
import gecko10000.geckolib.extensions.parseMM
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import redempt.redlib.inventorygui.InventoryGUI
import redempt.redlib.inventorygui.ItemButton

class AnvilHomeGUI(player: Player, private val block: Block) : MyKoinComponent, GUI(player) {

    /*companion object {
        private const val SIZE = 54
    } */

    private val plugin: BetterAnvils by inject()

    init {
        if (!Tag.ANVIL.isTagged(block.type)) {
            shouldOpen = false
        }
    }

    private fun enchantButton(): ItemButton {
        val item = ItemStack.of(Material.ENCHANTED_BOOK)
        item.editMeta {
            it.displayName(parseMM("<dark_purple>Enchantments"))
        }
        return ItemButton.create(item) { _ ->
            EnchantCombineGUI(player, block)
        }
    }

    private fun repairButton(): ItemButton {
        val item = ItemStack.of(block.type)
        item.editMeta {
            it.displayName(parseMM("<gray>Repair"))
        }
        return ItemButton.create(item) { _ ->
            ItemRepairGUI(player, block)
        }
    }

    override fun createInventory(): InventoryGUI {
        val inventory = InventoryGUI(Bukkit.createInventory(this, InventoryType.HOPPER, plugin.config.homeAnvilName))
        inventory.addButton(0, enchantButton())
        inventory.addButton(2, repairButton())
        return inventory
    }
}
