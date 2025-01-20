@file:UseSerializers(MMComponentSerializer::class)

package gecko10000.betteranvils.config

import gecko10000.geckolib.config.serializers.MMComponentSerializer
import gecko10000.geckolib.extensions.MM
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.kyori.adventure.text.Component

@Serializable
data class Config(
    val homeAnvilName: Component = MM.deserialize("<dark_gray>Anvil"),
    val enchantmentAnvilName: Component = MM.deserialize("<dark_purple>Enchantments"),
    val itemRepairName: Component = MM.deserialize("<dark_green>Item Repair"),
    val repairStartName: Component = MM.deserialize("<gold>Start Repair"),
)
