package gecko10000.geckoanvils.managers

import gecko10000.geckoanvils.GeckoAnvils
import gecko10000.geckoanvils.config.RepairEntry
import gecko10000.geckoanvils.di.MyKoinComponent
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration

@Suppress("UnstableApiUsage")
class RepairCombineManager : MyKoinComponent {

    private val plugin: GeckoAnvils by inject()

    /**
     * Since each item increases max durability as well
     * as repairing, we calculate the "damage lost" per item.
     * Then, use that to calculate the number of items needed for
     * full durability considering increases from max durability gains.
     *
     * Essentially, calculate how many items are needed to remove
     * all damage on the item.
     */
    private fun calcMaxPossibleNeededItems(itemDamage: Int, chosenRepair: RepairEntry): Int {
        val damageLostPerItem = chosenRepair.baseRepairAmount - chosenRepair.maxDurabilityIncrease
        return ceil(itemDamage.toDouble() / damageLostPerItem).toInt()
    }

    fun calculateCombination(inputItem: ItemStack, repairItem: ItemStack): CalcResult {
        // Item must be repairable
        val repairsForInput = plugin.repairConfig.repairs[inputItem.type] ?: return CalcResult.EMPTY
        // Repair item must be a candidate for the input
        val chosenRepair = repairsForInput.firstOrNull { it.matchedMaterials.materials.contains(repairItem.type) }
            ?: return CalcResult.EMPTY
        // Item must actually be damaged
        val itemDamageAmount = inputItem.getData(DataComponentTypes.DAMAGE) ?: 0
        if (itemDamageAmount == 0) return CalcResult.EMPTY

        val itemsUsed = min(repairItem.amount, calcMaxPossibleNeededItems(itemDamageAmount, chosenRepair))
        val damageRemoved = itemsUsed * chosenRepair.baseRepairAmount
        val previousMaxDamage = inputItem.getData(DataComponentTypes.MAX_DAMAGE)
            ?: inputItem.type.getDefaultData(DataComponentTypes.MAX_DAMAGE)
            ?: return CalcResult.EMPTY
        val maxDamageGained = chosenRepair.maxDurabilityIncrease * itemsUsed
        val newMaxDamage = previousMaxDamage + maxDamageGained
        val newDamageAmount = max(0, itemDamageAmount + maxDamageGained - damageRemoved)
        val actualDamageRemoved = itemDamageAmount - newDamageAmount
        val timeTaken = chosenRepair.time * itemsUsed
        val newItem = inputItem.clone()
        newItem.setData(DataComponentTypes.DAMAGE, newDamageAmount)
        newItem.setData(DataComponentTypes.MAX_DAMAGE, newMaxDamage)

        return CalcResult(newItem, actualDamageRemoved, maxDamageGained, itemsUsed, timeTaken)
    }

    data class CalcResult(
        val output: ItemStack?,
        val gainedDurability: Int,
        val gainedMaxDurability: Int,
        val materialsUsed: Int,
        val baseTime: Duration,
    ) {

        companion object {
            val EMPTY = CalcResult(null, 0, 0, 0, Duration.ZERO)
        }
    }

}
