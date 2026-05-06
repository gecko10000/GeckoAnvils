package gecko10000.geckoanvils.managers

import gecko10000.geckoanvils.GeckoAnvils
import gecko10000.geckoanvils.di.MyKoinComponent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Suppress("UnstableApiUsage")
class RepairCombineManager : MyKoinComponent {

    private val plugin: GeckoAnvils by inject()

    fun calculateCombination(inputItem: ItemStack, repairItem: ItemStack): CalcResult {
        return CalcResult(inputItem, 50, 5, 1.minutes)
    }

    data class CalcResult(
        val output: ItemStack?,
        val gainedDurability: Int,
        val gainedMaxDurability: Int,
        val time: Duration,
    ) {

        companion object {
            val EMPTY = CalcResult(null, 0, 0, Duration.ZERO)
        }
    }

}
