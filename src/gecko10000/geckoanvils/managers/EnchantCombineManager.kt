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
    private val permissionManager: PermissionManager by inject()

    /**
     * Only non-curses are removable.
     */
    private fun ItemStack.removableEnchants() = this.properEnchants().filterKeys { !it.isCursed }

    private fun ItemStack.properEnchants() =
        this.getData(DataComponentTypes.STORED_ENCHANTMENTS)?.enchantments() ?: this.enchantments

    private fun ItemStack.properContainsEnchant(enchantment: Enchantment): Boolean {
        return this.getData(DataComponentTypes.STORED_ENCHANTMENTS)?.enchantments()?.containsKey(enchantment)
            ?: this.containsEnchantment(enchantment)
    }

    /**
     * Adds enchants to the enchants map of a
     * normal item, or the stored enchantments
     * of an enchanted book.
     */
    private fun ItemStack.properAddEnchants(enchants: Map<Enchantment, Int>) {
        val storedEnchants = this.getData(DataComponentTypes.STORED_ENCHANTMENTS)
        if (storedEnchants != null) {
            this.setData(
                DataComponentTypes.STORED_ENCHANTMENTS,
                ItemEnchantments.itemEnchantments(
                    storedEnchants.enchantments().plus(enchants)
                )
            )
            return
        }
        this.addUnsafeEnchantments(enchants)
    }

    private fun calcDisenchantLevelCost(enchantsToRemove: Map<Enchantment, Int>): Int {
        val totalLevelCount = enchantsToRemove.values.sum()
        var overMaxPenalty = 1.0
        for ((enchant, level) in enchantsToRemove) {
            val levelsOverMax = max(1, level - enchant.maxLevel)
            overMaxPenalty += log2(levelsOverMax.toDouble())
        }
        val rawAmount = plugin.config.baseDisenchantLevelCost * totalLevelCount * overMaxPenalty
        return rawAmount.toInt()
    }

    private fun calcDisenchantTime(enchantsToRemove: Map<Enchantment, Int>): Duration {
        var overMaxPenalty = 1.0
        for ((enchant, level) in enchantsToRemove) {
            val levelsOverMax = max(0, level - enchant.maxLevel)
            overMaxPenalty += 2.0.pow(levelsOverMax) + level.toDouble() / enchant.maxLevel
        }
        val rawDuration = plugin.config.baseDisenchantDuration * overMaxPenalty
        return rawDuration
    }

    /**
     * Attempts to calculate the disenchantment result
     * when an enchanted item and a book are given.
     */
    private fun getDisenchantResult(input: List<ItemStack>): CalcResult? {
        // Needs to be just an item and a book
        if (input.size != 2) return null
        // One of the items is a book
        val book = input.firstOrNull { it.type == Material.BOOK }?.clone() ?: return null
        // And exactly 1
        if (book.amount != 1) return null

        // Ensure other item is not an enchanted book
        val otherItem = input.firstOrNull {
            it.type != Material.ENCHANTED_BOOK
        }?.clone() ?: return null
        // And exactly 1
        if (otherItem.amount != 1) return null
        val enchantsToRemove = otherItem.removableEnchants()
        // Item is disenchantable
        if (enchantsToRemove.isEmpty()) return null

        val outputBook = ItemStack.of(Material.ENCHANTED_BOOK)
        outputBook.properAddEnchants(enchantsToRemove)
        for ((enchant, _) in enchantsToRemove) {
            otherItem.removeEnchantment(enchant)
        }

        val levelCost = calcDisenchantLevelCost(enchantsToRemove)
        val time = calcDisenchantTime(enchantsToRemove)
        return CalcResult(
            listOf(outputBook, otherItem),
            levelCost = levelCost,
            baseTime = time,
        )
    }

    /**
     * Ensures input items are either all enchanted books
     * or all the same item + enchanted books mixed in.
     * Returns the first item so any data from it is in the output.
     */
    private fun getBaseOutputItem(input: List<ItemStack>): ItemStack? {
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

    /**
     * Performs combination logic. Counts number of
     * each enchant level across given items, and
     * takes (highest level + 1) if there are 2+
     * items with that level.
     */
    private fun getOutputEnchants(inputs: List<ItemStack>): Map<Enchantment, Int> {
        // count
        val enchantLevelCounts = mutableMapOf<Enchantment, MutableMap<Int, Int>>()
        for (inputItem in inputs) {
            for ((enchant, level) in inputItem.properEnchants()) {
                enchantLevelCounts
                    .computeIfAbsent(enchant) { mutableMapOf() }
                    .compute(level) { _, prev -> (prev ?: 0) + 1 }
            }
        }

        // calculate output
        val outputEnchants = mutableMapOf<Enchantment, Int>()
        for (enchant in enchantLevelCounts.keys) {
            val enchantCountMap = enchantLevelCounts.getValue(enchant)
            val highestLevel = enchantCountMap.maxOf { it.key }
            val enchantLevel = if (enchantCountMap.getValue(highestLevel) >= 2) highestLevel + 1 else highestLevel
            outputEnchants[enchant] = enchantLevel
        }
        return outputEnchants
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

    private fun getRemainingDurability(item: ItemStack): Int {
        val maxDamage = item.getData(DataComponentTypes.MAX_DAMAGE) ?: return 0
        if (maxDamage == 0) return 0
        val damage = item.getData(DataComponentTypes.DAMAGE) ?: 0
        return maxDamage - damage
    }

    private fun combineDurability(inputs: List<ItemStack>, output: ItemStack): ItemStack {
        val outputMaxDamage = output.getData(DataComponentTypes.MAX_DAMAGE) ?: return output
        if ((output.getData(DataComponentTypes.DAMAGE) ?: 0) == 0) return output
        val newDurability = inputs.sumOf(this::getRemainingDurability)
        val newDamage = max(outputMaxDamage - newDurability, 0)
        val copy = output.clone()
        copy.setData(DataComponentTypes.DAMAGE, newDamage)
        return copy
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

    /**
     * For each enchant, add to the total the following:
     * - below max? base cost * level * (1 + level/max)
     * - above max? base cost * level * 2^(# levels over + 1)
     *
     * Finally, divide by (log_2(n+1)+1)/2 where n is
     * the number of enchants. This softens stacked enchants a bit.
     */
    private fun getCombineLevelCost(outputItem: ItemStack): Int {
        var xpCost = 0.0
        val enchants = outputItem.properEnchants()
        for ((enchant, level) in enchants) {
            var costForEnchant = plugin.config.baseEnchantmentCost.toDouble() * level
            // [1, max]: 1+level/max: (1, 2]
            // (max, inf): 2^(level+1 - max): (4, 8, ...)
            val isWithinVanilla = level <= enchant.maxLevel
            if (isWithinVanilla) {
                costForEnchant *= (1 + level.toDouble() / enchant.maxLevel).pow(enchants.size)
            } else {
                costForEnchant *= 2.0.pow(level + 1 - enchant.maxLevel.toDouble())
            }
            xpCost += costForEnchant
        }
        xpCost /= (log2(enchants.size + 1.toDouble()) + 1) / 2
        return xpToLevel(ceil(xpCost).toInt())
    }

    /**
     * For each enchant, add to the total the following:
     * - below max? base duration * (level / max) * level
     * - above max? base duration * (level / max) * 25 * 2^(# levels over)
     *
     * TODO: make the 25 configurable
     */
    private fun getCombineTime(outputItem: ItemStack): Duration {
        var duration = Duration.ZERO
        val enchants = outputItem.properEnchants()
        for ((enchant, level) in enchants) {
            var enchantDur = plugin.config.baseEnchantmentDuration
            enchantDur *= (level.toDouble() / enchant.maxLevel)
            if (level > enchant.maxLevel) {
                val aboveVanilla = level - enchant.maxLevel
                val overLevelPenalty = 2.0.pow(aboveVanilla)
                enchantDur *= overLevelPenalty * 5
            }
            duration += enchantDur
        }
        duration *= (log2(enchants.size + 1.toDouble()) + 1) / 2
        return duration
    }

    /**
     * Calculates enchantment upgrades and which enchantments
     * to apply. Returns the calculation's result, if any.
     */
    fun calculateCombination(inputs: List<ItemStack>): CalcResult {
        val disenchantResult = getDisenchantResult(inputs)
        if (disenchantResult != null) return disenchantResult
        val outputItem = getBaseOutputItem(inputs) ?: return CalcResult.EMPTY
        val outputEnchants = getOutputEnchants(inputs)
        val validEnchants = keepApplicableEnchants(outputItem, outputEnchants)
        if (validEnchants.isEmpty()) return CalcResult.EMPTY
        val combinedDurability = combineDurability(inputs, outputItem)
        val newItem = applyEnchants(combinedDurability, validEnchants)
        if (inputs.any { it.isSimilar(newItem) }) return CalcResult.EMPTY
        val levelCost = getCombineLevelCost(newItem)
        val time = getCombineTime(newItem)
        return CalcResult(listOf(newItem), levelCost, time)
    }

    data class CalcResult(
        val output: List<ItemStack>?,
        val levelCost: Int,
        val baseTime: Duration,
    ) {
        companion object {
            val EMPTY = CalcResult(null, 0, Duration.ZERO)
        }

    }

}
