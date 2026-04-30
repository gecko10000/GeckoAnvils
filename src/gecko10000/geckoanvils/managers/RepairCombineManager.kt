package gecko10000.geckoanvils.managers

import gecko10000.geckoanvils.di.MyKoinComponent
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.inventory.ItemStack
import kotlin.time.Duration

@Suppress("UnstableApiUsage")
class RepairCombineManager : MyKoinComponent {

    data class CalcResult(
        val output: ItemStack?,
        val gainedMaxDurability: Int,
        val time: Duration,
    ) {
        // Note that since we are increasing max damage,
        // the durability will also increase.
        fun withGainedMaxDurability(): ItemStack? {
            output ?: return null
            val copy = output.clone()
            val currentMax = copy.getData(DataComponentTypes.MAX_DAMAGE) ?: copy.type.maxDurability.toInt()
            copy.setData(DataComponentTypes.MAX_DAMAGE, currentMax + gainedMaxDurability)
            return copy
        }

        companion object {
            val EMPTY = CalcResult(null, 0, Duration.ZERO)
        }
    }

}
