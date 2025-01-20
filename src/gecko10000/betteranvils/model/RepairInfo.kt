@file:UseSerializers(InternalItemStackSerializer::class)

package gecko10000.betteranvils.model

import gecko10000.geckolib.config.serializers.InternalItemStackSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@Serializable
data class RepairInfo(
    val item: ItemStack,
    val startTime: Long,
    val repairMaterial: Material,
    val repairAmount: Int,
    val repairDuration: Long,
)
