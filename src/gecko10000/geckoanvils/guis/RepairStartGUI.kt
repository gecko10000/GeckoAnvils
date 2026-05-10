package gecko10000.geckoanvils.guis

import gecko10000.geckoanvils.DurationUtils
import gecko10000.geckoanvils.GeckoAnvils
import gecko10000.geckoanvils.config.RepairEntry
import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckoanvils.managers.AnvilBlockManager
import gecko10000.geckoanvils.managers.DataManager
import gecko10000.geckoanvils.managers.PermissionManager
import gecko10000.geckoanvils.managers.RepairCombineManager
import gecko10000.geckoanvils.model.RepairInfo
import gecko10000.geckolib.extensions.*
import gecko10000.geckolib.inventorygui.InventoryGUI
import gecko10000.geckolib.inventorygui.ItemButton
import gecko10000.geckolib.misc.Task
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import kotlin.math.min
import kotlin.time.Duration

class RepairStartGUI(player: Player, block: Block, private val index: Int) : MyKoinComponent,
    AnvilAssociatedGUI(player, block) {

    companion object {
        private const val SIZE = 36
        private const val INPUT_ITEM_SLOT = 10
        private const val REPAIR_ITEM_SLOT = 13
        private const val RESULT_SLOT = 16
        private const val TIME_SLOT = 7
        private const val CONFIRM_SLOT = 25
        private val INGREDIENT_SUGGESTION_SLOTS = listOf(30, 31, 32, 33)
        private val INPUT_HIGHLIGHT_ITEM = ItemStack.of(Material.CYAN_STAINED_GLASS_PANE)
            .also { it.editMeta { it.itemName(parseMM("<dark_aqua>Item to Repair")) } }
        private val REPAIR_HIGHLIGHT_ITEM = ItemStack.of(Material.YELLOW_STAINED_GLASS_PANE)
            .also { it.editMeta { it.itemName(parseMM("<yellow>Material to Repair With")) } }
    }

    private val plugin: GeckoAnvils by inject()
    private val anvilBlockManager: AnvilBlockManager by inject()
    private val repairCombineManager: RepairCombineManager by inject()
    private val dataManager: DataManager by inject()
    private val permissionManager: PermissionManager by inject()

    private var itemToRepair: ItemStack?
        get() = inventory.inventory.getItem(INPUT_ITEM_SLOT)
        set(value) = inventory.inventory.setItem(INPUT_ITEM_SLOT, value)
    private var sacrificedItem: ItemStack?
        get() = inventory.inventory.getItem(REPAIR_ITEM_SLOT)
        set(value) = inventory.inventory.setItem(REPAIR_ITEM_SLOT, value)
    private var result: RepairCombineManager.CalcResult? = null

    private fun getSpeedup() = permissionManager.getEnchantTimeSpeedup(player)

    private fun timeItem(time: Duration): ItemStack {
        val speedup = getSpeedup()
        val item = ItemStack.of(Material.CLOCK)
        item.editMeta {
            if (speedup == 1.0 || time == Duration.ZERO) {
                it.displayName(parseMM("<dark_aqua>Time: <aqua><b>${DurationUtils.format(time)}"))
            } else {
                val original = DurationUtils.format(time)
                val actual = DurationUtils.format(time / speedup)
                it.displayName(parseMM("<dark_aqua>Time: <aqua><b>$actual</b><dark_aqua> (<green>${speedup}x</green> speedup)"))
                it.lore(listOf(parseMM("<dark_aqua>Original: <red>$original")))
            }
        }
        return item
    }

    private fun repairEntryItem(repairEntry: RepairEntry): ItemStack {
        val materials = repairEntry.matchedMaterials.materials.toList()
        val item = ItemStack.of(materials[0])
        item.editMeta { meta ->
            val otherOptions = materials.subList(1, materials.size)
            val lore = buildList {
                addAll(otherOptions.map {
                    MM.deserialize(
                        "<dark_aqua>or</dark_aqua> <material>",
                        Placeholder.component("material", Component.translatable(it.translationKey()))
                    ).withDefaults()
                })
                add(Component.empty())
                if (repairEntry.baseRepairAmount > 0) {
                    add(parseMM("<yellow>+${repairEntry.baseRepairAmount} durability"))
                }
                if (repairEntry.maxDurabilityIncrease > 0) {
                    add(parseMM("<gold>+${repairEntry.maxDurabilityIncrease} max durability"))
                }
                val originalTime = repairEntry.time
                val time = originalTime / getSpeedup()
                add(parseMM("<aqua>${DurationUtils.format(time)} <dark_aqua>each"))
            }
            meta.lore(lore)
        }
        return item
    }

    private fun resultItem(inventory: InventoryGUI): ItemStack? {
        val itemToRepair = inventory.inventory.getItem(INPUT_ITEM_SLOT)
        val sacrificedItem = inventory.inventory.getItem(REPAIR_ITEM_SLOT)
        if (itemToRepair.isEmpty() || sacrificedItem.isEmpty()) {
            this.result = null
            return null
        }
        itemToRepair!!; sacrificedItem!!
        val result = repairCombineManager.calculateCombination(itemToRepair, sacrificedItem)
        this.result = if (result.output == null) null else result
        val displayedItem = result.output?.clone() ?: return null
        val maxDamage = result.output.getData(DataComponentTypes.MAX_DAMAGE) ?: result.output.type.maxDurability.toInt()
        val durability = maxDamage - (result.output.getData(DataComponentTypes.DAMAGE) ?: 0)
        displayedItem.lore(
            (displayedItem.lore() ?: emptyList())
                .plus(Component.empty())
                .plus(parseMM("<yellow>+${result.gainedDurability} durability"))
                .plus(parseMM("<gold>+${result.gainedMaxDurability} max durability"))
                .plus(parseMM("<green>Result: $durability / $maxDamage"))
        )
        return displayedItem
    }

    private fun confirmButton(): ItemButton? {
        val type = this.block.type
        if (!type.name.endsWith("ANVIL")) {
            val item = ItemStack.of(Material.BARRIER)
            item.editMeta {
                it.itemName(parseMM("<red>The anvil is missing, dummy..."))
            }
            return ItemButton.create(item) {}
        }
        val item = ItemStack.of(type)
        val result = this.result ?: return null
        val sacrificedItem = this.sacrificedItem ?: return null
        val actualTime = result.baseTime / getSpeedup()
        item.editMeta {
            it.itemName(parseMM("<green>Confirm"))
            it.lore(
                listOf(
                    MM.deserialize(
                        "<red>This will cost <yellow><u><amount> <item>",
                        Placeholder.unparsed("amount", result.materialsUsed.toString()),
                        Placeholder.component("item", Component.translatable(sacrificedItem.type.translationKey()))
                    ).withDefaults(),
                    MM.deserialize(
                        "<red>and take <aqua><u><time>",
                        Placeholder.unparsed("time", DurationUtils.format(actualTime))
                    ).withDefaults()
                )
            )
        }
        return ItemButton.create(item) { _ ->
            if (!anvilBlockManager.isValid(block)) {
                player.closeInventory()
                return@create
            }
            anvilBlockManager.damageAnvil(block)
            val prevData = dataManager.getData(player)
            val inputItem = inventory.inventory.getItem(INPUT_ITEM_SLOT)?.clone() ?: return@create
            val repairItem = inventory.inventory.getItem(REPAIR_ITEM_SLOT) ?: return@create
            val outputItem = result.output ?: return@create
            val updatedRepairs = prevData.currentRepairs.extend(null, targetAmount = index + 1).updated(index) {
                RepairInfo(
                    inputItem = inputItem,
                    startTime = System.currentTimeMillis(),
                    duration = result.baseTime.inWholeMilliseconds,
                    repairMaterial = repairItem.type,
                    repairAmount = result.materialsUsed,
                    outputItem = outputItem,
                )
            }
            dataManager.setData(player, prevData.copy(currentRepairs = updatedRepairs))
            inventory.setReturnsItems(false)
            val amountToReturn = repairItem.amount - result.materialsUsed
            if (amountToReturn > 0) {
                player.give(repairItem.asQuantity(amountToReturn))
            }
            ItemRepairGUI(player, block)
        }
    }

    private val noRepairItem = ItemStack.of(Material.BARRIER).apply {
        editMeta {
            it.displayName(parseMM("<red>Can't be repaired!"))
        }
    }

    private fun updateInventory(inventory: InventoryGUI = this.inventory) {
        val itemToRepair = inventory.inventory.getItem(INPUT_ITEM_SLOT)
        val repairEntries = plugin.repairConfig.repairs[itemToRepair?.type]
        INGREDIENT_SUGGESTION_SLOTS.forEach { inventory.inventory.setItem(it, FILLER) }
        if (itemToRepair != null) {
            if (repairEntries != null) {
                for (i in 0..<min(repairEntries.size, INGREDIENT_SUGGESTION_SLOTS.size)) {
                    inventory.inventory.setItem(INGREDIENT_SUGGESTION_SLOTS[i], repairEntryItem(repairEntries[i]))
                }
            } else {
                inventory.inventory.setItem(INGREDIENT_SUGGESTION_SLOTS[1], noRepairItem)
            }
        }
        val resultItem = resultItem(inventory)
        inventory.inventory.setItem(RESULT_SLOT, resultItem)
        inventory.inventory.setItem(TIME_SLOT, result?.baseTime?.let { timeItem(it) } ?: FILLER)
        val confirmButton = confirmButton()
        if (confirmButton == null) {
            inventory.getButton(CONFIRM_SLOT)?.let(inventory::removeButton)
            inventory.inventory.setItem(CONFIRM_SLOT, FILLER)
        } else {
            inventory.addButton(CONFIRM_SLOT, confirmButton)
        }
    }

    override fun createInventory(): InventoryGUI {
        val inventory = InventoryGUI(Bukkit.createInventory(this, SIZE, plugin.config.repairStartName))
        inventory.fill(0, SIZE, FILLER)
        inventory.fill(0, 0, 3, 3, INPUT_HIGHLIGHT_ITEM)
        inventory.fill(3, 0, 6, 3, REPAIR_HIGHLIGHT_ITEM)
        for (slot in setOf(INPUT_ITEM_SLOT, REPAIR_ITEM_SLOT)) {
            inventory.inventory.setItem(slot, null)
            inventory.openSlot(slot)
        }
        inventory.addButton(SIZE - 9, BACK { ItemRepairGUI(player, block) })
        inventory.setOnClickOpenSlot { _ -> Task.syncDelayed { -> updateInventory() } }
        inventory.setOnDragOpenSlot { _ -> Task.syncDelayed { -> updateInventory() } }
        updateInventory(inventory)
        return inventory
    }
}
