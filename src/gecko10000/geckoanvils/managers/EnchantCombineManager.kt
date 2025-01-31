package gecko10000.geckoanvils.managers

import gecko10000.geckoanvils.GeckoAnvils
import gecko10000.geckoanvils.di.MyKoinComponent
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemEnchantments
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import kotlin.math.*
import kotlin.time.Duration

@Suppress("UnstableApiUsage")
class EnchantCombineManager : MyKoinComponent {

    private val plugin: GeckoAnvils by inject()

    private fun ItemStack.properEnchants(): Map<Enchantment, Int> {
        return this.getData(DataComponentTypes.STORED_ENCHANTMENTS)?.enchantments() ?: this.enchantments
    }

    private fun ItemStack.properContainsEnchant(enchantment: Enchantment): Boolean {
        return this.getData(DataComponentTypes.STORED_ENCHANTMENTS)?.enchantments()?.containsKey(enchantment)
            ?: this.containsEnchantment(enchantment)
    }

    private fun ItemStack.properAddEnchants(enchants: Map<Enchantment, Int>) {
        val storedEnchants = this.getData(DataComponentTypes.STORED_ENCHANTMENTS)
        if (storedEnchants != null) {
            this.setData(
                DataComponentTypes.STORED_ENCHANTMENTS,
                ItemEnchantments.itemEnchantments(
                    storedEnchants.enchantments().plus(enchants),
                    storedEnchants.showInTooltip()
                )
            )
            return
        }
        this.addUnsafeEnchantments(enchants)
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
        item.properAddEnchants(validEnchants)
        return item
    }

    // https://minecraft.wiki/w/Experience#Leveling_up
    private fun xpToLevel(xp: Int): Int {
        return ceil(
            when (xp) {
                in 0..352 -> sqrt(xp + 9.0) - 3
                in 353..1507 -> 8.1 + sqrt(2.0 / 5 * (xp - 7839.0 / 40))
                else -> 325.0 / 18 + sqrt(2.0 / 9 * (xp - 54215.0 / 72))
            }
        ).toInt()
    }

    private fun getLevelCost(item: ItemStack): Int {
        var xpCost = 0.0
        val enchants = item.properEnchants()
        for ((enchant, level) in enchants) {
            var costForEnchant = plugin.config.baseEnchantmentCost.toDouble() * level
            // [1, max]: 1+level/max: (1, 2]
            // (max, inf): 2^(level+1 - max): (4, 8, ...)
            val isWithinVanilla = level <= enchant.maxLevel
            if (isWithinVanilla) {
                costForEnchant *= 1 + level.toDouble() / enchant.maxLevel
            } else {
                costForEnchant *= 2.0.pow(level + 1 - enchant.maxLevel.toDouble())
            }
            xpCost += costForEnchant
        }
        xpCost *= enchants.size
        return xpToLevel(ceil(xpCost).toInt())
    }

    private fun getTime(item: ItemStack): Duration {
        var duration = Duration.ZERO
        val enchants = item.properEnchants()
        for ((enchant, level) in enchants) {
            var enchantDur = plugin.config.baseEnchantmentDuration
            val withinMax = min(enchant.maxLevel, level) + 1
            enchantDur = enchantDur.times(withinMax)
            val extra = max(level - enchant.maxLevel, 0)
            val extraMult = 2.0.pow(extra)
            enchantDur = enchantDur.times(extraMult)
            duration = duration.plus(enchantDur)
        }
        return duration
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
        if (input.any { it.isSimilar(newItem) }) return CalcResult.EMPTY
        val levelCost = getLevelCost(newItem)
        val time = getTime(newItem)
        return CalcResult(listOf(newItem), levelCost, time)
    }

    data class CalcResult(
        val output: List<ItemStack>?,
        val levelCost: Int,
        val time: Duration,
    ) {
        companion object {
            val EMPTY = CalcResult(null, 0, Duration.ZERO)
        }
    }

}
