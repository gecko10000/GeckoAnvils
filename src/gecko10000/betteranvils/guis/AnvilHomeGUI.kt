package gecko10000.betteranvils.guis

import gecko10000.betteranvils.BetterAnvils
import gecko10000.betteranvils.di.MyKoinComponent
import gecko10000.geckolib.extensions.parseMM
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import redempt.redlib.inventorygui.InventoryGUI
import redempt.redlib.inventorygui.ItemButton

class AnvilHomeGUI(player: Player, block: Block) : MyKoinComponent, AnvilAssociatedGUI(player, block) {

    /*companion object {
        private const val SIZE = 54
    } */

    private val plugin: BetterAnvils by inject()

    private fun enchantButton(): ItemButton {
        val item = ItemStack.of(Material.ENCHANTED_BOOK)
        item.editMeta {
            it.displayName(parseMM("<dark_purple>Enchant"))
        }
        return ItemButton.create(item) { _ ->
            EnchantGUI(player, block)
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

    private fun renameButton(): ItemButton {
        val item = ItemStack.of(Material.NAME_TAG)
        item.editMeta {
            it.displayName(parseMM("<yellow>Rename"))
        }
        return ItemButton.create(item) { _ ->
            AnvilRenameGUI(player, block)
        }
    }

    override fun createInventory(): InventoryGUI {
        val inventory = InventoryGUI(Bukkit.createInventory(this, InventoryType.HOPPER, plugin.config.homeAnvilName))
        inventory.fill(0, inventory.size, FILLER)
        inventory.addButton(0, enchantButton())
        inventory.addButton(2, repairButton())
        inventory.addButton(4, renameButton())
        return inventory
    }
}
