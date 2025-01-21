package gecko10000.betteranvils.guis

import gecko10000.betteranvils.BetterAnvils
import gecko10000.betteranvils.di.MyKoinComponent
import gecko10000.betteranvils.managers.AnvilBlockManager
import gecko10000.betteranvils.managers.DataManager
import gecko10000.betteranvils.model.EnchantInfo
import gecko10000.geckolib.extensions.extend
import gecko10000.geckolib.extensions.updated
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import redempt.redlib.inventorygui.InventoryGUI
import redempt.redlib.inventorygui.ItemButton

class EnchantStartGUI(player: Player, block: Block, private val index: Int) : MyKoinComponent,
    AnvilAssociatedGUI(player, block) {

    companion object {
        private const val SIZE = 54
    }

    private val plugin: BetterAnvils by inject()
    private val anvilBlockManager: AnvilBlockManager by inject()
    private val dataManager: DataManager by inject()

    private fun testButton(): ItemButton {
        val item = ItemStack.of(Material.GREEN_STAINED_GLASS)
        return ItemButton.create(item) { _ ->
            if (!anvilBlockManager.isValid(block)) {
                player.closeInventory()
                return@create
            }
            val prevData = dataManager.getData(player)
            val updatedEnchants = prevData.currentEnchants.extend(null, targetAmount = index + 1).updated(index) {
                EnchantInfo(
                    listOf(),
                    startTime = System.currentTimeMillis(),
                    duration = 1000 * 30L,
                    outputItem = ItemStack.of(Material.DIAMOND),
                )
            }
            dataManager.setData(player, prevData.copy(currentEnchants = updatedEnchants))
            anvilBlockManager.damageAnvil(block)
            EnchantGUI(player, block)
        }
    }

    override fun createInventory(): InventoryGUI {
        val inventory = InventoryGUI(Bukkit.createInventory(this, SIZE, plugin.config.enchantmentStartName))
        inventory.fill(0, SIZE, FILLER)
        inventory.addButton(0, testButton())
        inventory.addButton(SIZE - 9, BACK { EnchantGUI(player, block) })
        return inventory
    }
}
