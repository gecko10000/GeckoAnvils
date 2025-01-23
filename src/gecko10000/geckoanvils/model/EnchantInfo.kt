@file:UseSerializers(InternalItemStackSerializer::class)

package gecko10000.geckoanvils.model

import gecko10000.geckolib.config.serializers.InternalItemStackSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.inventory.ItemStack

// baseItem can be null
@Serializable
data class EnchantInfo(
    val inputItems: List<ItemStack>,
    val startTime: Long,
    val duration: Long,
    val outputItem: ItemStack,
)
