@file:UseSerializers(
    DurationSerializer::class,
)

package gecko10000.geckoanvils.config

import gecko10000.geckolib.config.serializers.DurationSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Duration

@Serializable
data class RepairEntry(
    val matchedMaterials: RegexMaterialPredicate,
    val baseRepairAmount: Int,
    val maxDurabilityIncrease: Int,
    val time: Duration,
) {
    companion object {
    }

}
