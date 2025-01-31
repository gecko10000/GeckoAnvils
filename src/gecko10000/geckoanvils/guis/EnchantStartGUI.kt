package gecko10000.geckoanvils.guis

import gecko10000.geckoanvils.DurationUtils
import gecko10000.geckoanvils.GeckoAnvils
import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckoanvils.managers.AnvilBlockManager
import gecko10000.geckoanvils.managers.DataManager
import gecko10000.geckoanvils.managers.EnchantCombineManager
import gecko10000.geckoanvils.model.EnchantInfo
import gecko10000.geckolib.extensions.*
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import redempt.redlib.inventorygui.InventoryGUI
import redempt.redlib.inventorygui.ItemButton
import redempt.redlib.misc.Task
import kotlin.time.Duration

class EnchantStartGUI(player: Player, block: Block, private val index: Int) : MyKoinComponent,
    AnvilAssociatedGUI(player, block) {

    companion object {
        private const val SIZE = 54
        private const val XP_COST_SLOT = 48
        private const val OUTPUT_SLOT = 49
        private const val TIME_SLOT = 50
    }

    private val plugin: GeckoAnvils by inject()
    private val anvilBlockManager: AnvilBlockManager by inject()
    private val dataManager: DataManager by inject()
    private val enchantCombineManager: EnchantCombineManager by inject()

    private var result: EnchantCombineManager.CalcResult = EnchantCombineManager.CalcResult.EMPTY

    private val noOutputItem = ItemStack.of(Material.BARRIER).apply {
        editMeta {
            it.displayName(parseMM("<red>No Result!"))
        }
    }

    private fun levelCostItem(cost: Int): ItemStack {
        val item = ItemStack.of(Material.EXPERIENCE_BOTTLE)
        val level = player.level
        item.editMeta {
            it.displayName(parseMM("<gold>Level Cost: <yellow>$cost"))
            it.lore(listOf(parseMM("<gold>You have <yellow>$level")))
        }
        return item
    }

    private fun timeItem(time: Duration): ItemStack {
        val item = ItemStack.of(Material.CLOCK)
        item.editMeta {
            it.displayName(parseMM("<dark_aqua>Time: ${DurationUtils.format(time)}"))
        }
        return item
    }

    private fun validConfirmButton(): ItemButton {
        val item = ItemStack.of(Material.LIME_STAINED_GLASS_PANE)
        item.editMeta {
            it.displayName(parseMM("<green>Confirm"))
            it.lore(
                listOf(
                    parseMM("<red>This will cost <u>${result.levelCost} levels"),
                    parseMM("<red>and take <u>${DurationUtils.format(result.time)}</u>.")
                )
            )
        }
        return ItemButton.create(item) { _ ->
            if (!anvilBlockManager.isValid(block)) {
                player.closeInventory()
                return@create
            }
            anvilBlockManager.damageAnvil(block)
            player.level -= result.levelCost
            val prevData = dataManager.getData(player)
            val updatedEnchants = prevData.currentEnchants.extend(null, targetAmount = index + 1).updated(index) {
                EnchantInfo(
                    inventory.openSlots.sorted().mapNotNull { inventory.inventory.getItem(it) },
                    startTime = System.currentTimeMillis(),
                    duration = result.time.inWholeMilliseconds,
                    outputItem = result.output!!,
                )
            }
            dataManager.setData(player, prevData.copy(currentEnchants = updatedEnchants))
            EnchantGUI(player, block)
        }
    }

    private fun invalidConfirmButton(message: String): ItemButton {
        val item = ItemStack.of(Material.YELLOW_STAINED_GLASS_PANE)
        item.editMeta {
            it.displayName(
                MM.deserialize(
                    "<yellow><message>",
                    Placeholder.unparsed("message", message)
                ).withDefaults()
            )
        }
        return ItemButton.create(item) { _ -> }
    }

    private fun confirmButton(): ItemButton {
        if (result.output == null) return invalidConfirmButton("No valid combination of items provided!")
        if (player.level < result.levelCost) return invalidConfirmButton("You don't have enough XP levels!")
        return validConfirmButton()
    }

    private fun updateInventory(gui: InventoryGUI = inventory) {
        val inputs = gui.openSlots.sorted().mapNotNull { gui.inventory.getItem(it) }
        result = enchantCombineManager.calculateCombination(inputs)
        gui.inventory.setItem(XP_COST_SLOT, levelCostItem(result.levelCost))
        gui.inventory.setItem(OUTPUT_SLOT, result.output ?: noOutputItem)
        gui.inventory.setItem(TIME_SLOT, timeItem(result.time))
        gui.addButton(SIZE - 1, confirmButton())
    }

    override fun createInventory(): InventoryGUI {
        val inventory = InventoryGUI(Bukkit.createInventory(this, SIZE, plugin.config.enchantmentStartName))
        inventory.fill(0, SIZE, FILLER)
        inventory.fill(1, 1, 8, 4, null)
        inventory.openSlots(1, 1, 8, 4)
        inventory.setOnClickOpenSlot { _ -> Task.syncDelayed { -> updateInventory() } }
        inventory.setOnDragOpenSlot { _ -> Task.syncDelayed { -> updateInventory() } }
        inventory.addButton(SIZE - 9, BACK { EnchantGUI(player, block) })
        updateInventory(inventory)
        return inventory
    }
}
