package gecko10000.geckoanvils.managers

import gecko10000.geckoanvils.di.MyKoinComponent
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import kotlin.math.max

@Suppress("UnstableApiUsage")
class EnchantCombineManager : MyKoinComponent {

    private fun ItemStack.properEnchants(): Map<Enchantment, Int> {
        return this.getData(DataComponentTypes.STORED_ENCHANTMENTS)?.enchantments() ?: this.enchantments
    }

    private fun ItemStack.properContainsEnchant(enchantment: Enchantment): Boolean {
        return this.getData(DataComponentTypes.STORED_ENCHANTMENTS)?.enchantments()?.containsKey(enchantment)
            ?: this.containsEnchantment(enchantment)
    }

    // Checks to make sure the input items
    // consist entirely of enchanted books
    // or the same item type. Returns the
    // output item, or null if the input
    // is invalid.
    private fun areInputMaterialsValid(input: List<ItemStack>): ItemStack? {
        var outputItem: ItemStack? = null
        for (item in input) {
            val type = item.type
            // Ignore enchanted books
            if (type == Material.ENCHANTED_BOOK) {
                continue
            }
            // First occurrence of non-book
            if (outputItem == null) {
                outputItem = item.clone()
                continue
            }
            // Ensure all non-books are the same type
            if (outputItem.type != type) return null
        }
        return outputItem ?: ItemStack.of(Material.ENCHANTED_BOOK)
    }

    private fun getEnchantCounts(input: List<ItemStack>): Map<Enchantment, Map<Int, Int>> {
        // We want to preserve insertion order to keep
        // the first enchant in `keepApplicableEnchants`
        val enchantCounts: MutableMap<Enchantment, MutableMap<Int, Int>> = LinkedHashMap()
        for (item in input) {
            for (ench in item.properEnchants()) {
                val map = enchantCounts.computeIfAbsent(ench.key) { mutableMapOf() }
                map.compute(ench.value) { _, v -> (v ?: 0) + 1 }
            }
        }
        return enchantCounts
    }

    private fun getMaxLevel(counts: Map<Int, Int>): Int {
        var highest = 0
        for ((level, count) in counts) {
            highest = max(highest, if (count > level) level + 1 else level)
        }
        return highest
    }

    private fun getOutputEnchants(enchantCounts: Map<Enchantment, Map<Int, Int>>): Map<Enchantment, Int> {
        return enchantCounts.map { it.key to getMaxLevel(it.value) }.toMap()
    }

    private fun keepApplicableEnchants(item: ItemStack, outputEnchants: Map<Enchantment, Int>): Map<Enchantment, Int> {
        val finalEnchants = mutableMapOf<Enchantment, Int>()
        for ((ench, level) in outputEnchants) {
            // We already have a conflicting enchantment
            if (finalEnchants.any { it.key.conflictsWith(ench) }) continue
            // Enchantment doesn't go on output
            if (item.type != Material.ENCHANTED_BOOK && !ench.canEnchantItem(item)) continue
            finalEnchants[ench] = level
        }
        return finalEnchants
    }

    private fun applyEnchants(item: ItemStack, validEnchants: Map<Enchantment, Int>): ItemStack {
        val item = item.clone()
        item.addUnsafeEnchantments(validEnchants)
        return item
    }

    // Calculates enchantment upgrades and where to apply
    // enchantments. Returns the result (possibly none) and
    // whether any upgrades were performed
    fun calculateCombination(input: List<ItemStack>): CalcResult {
        val outputItem = areInputMaterialsValid(input) ?: return CalcResult.EMPTY
        val enchantCounts = getEnchantCounts(input)
        val outputEnchants = getOutputEnchants(enchantCounts)
        val validEnchants = keepApplicableEnchants(outputItem, outputEnchants)
        if (validEnchants.isEmpty()) return CalcResult.EMPTY
        val newItem = applyEnchants(outputItem, validEnchants)
        if (newItem.isSimilar(outputItem)) return CalcResult.EMPTY
        return CalcResult(newItem, 100, 2.0)
    }

    private data class LeveledEnch(
        val enchantment: Enchantment,
        val level: Int,
    )

    data class CalcResult(
        val output: ItemStack?,
        val xpCost: Int,
        val timeMultiplier: Double,
    ) {
        companion object {
            val EMPTY = CalcResult(null, 0, 1.0)
        }
    }

}
