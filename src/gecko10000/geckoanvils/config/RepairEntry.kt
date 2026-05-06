package gecko10000.geckoanvils.config

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.floor

@Serializable
data class RepairEntry(
    val matchedMaterials: RegexMaterialPredicate,
    val baseRepairAmount: Int,
    val maxDurabilityIncrease: Double,
) {
    companion object {
        private const val EPSILON = 0.0001
    }

    fun cleanMaxDurabilityIncrease(): String {
        val floored = floor(maxDurabilityIncrease)
        if (abs(floored - maxDurabilityIncrease) < EPSILON) {
            return maxDurabilityIncrease.toInt().toString()
        }
        return maxDurabilityIncrease.toString()
    }

}
