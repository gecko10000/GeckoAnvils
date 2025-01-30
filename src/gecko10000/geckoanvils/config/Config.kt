@file:UseSerializers(MMComponentSerializer::class, DurationSerializer::class)

package gecko10000.geckoanvils.config

import com.charleskorn.kaml.YamlComment
import gecko10000.geckolib.config.serializers.DurationSerializer
import gecko10000.geckolib.config.serializers.MMComponentSerializer
import gecko10000.geckolib.extensions.MM
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.kyori.adventure.text.Component
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
data class Config(
    val homeAnvilName: Component = MM.deserialize("<dark_gray>Anvil"),
    val enchantmentAnvilName: Component = MM.deserialize("<dark_purple>Enchantments"),
    val enchantmentStartName: Component = MM.deserialize("<#008B8B>Enchant Item or Combine Books"),
    val itemRepairName: Component = MM.deserialize("<dark_green>Item Repair"),
    val repairStartName: Component = MM.deserialize("<#008B8B>Start Repair"),
    val renameGUIName: Component = MM.deserialize("<gradient:blue:dark_green>Item Rename"),
    val simpleRenameCost: Int = 15,
    val coloredRenameCost: Int = 30,
    @YamlComment(
        "Attempted renaming with these MM",
        "tags will be burned with fire."
    )
    val selfDestructTags: List<String> = listOf("clickEvent"),
    val baseEnchantmentCost: Int = 100,
    val baseEnchantmentDuration: Duration = 10.minutes,
    val progressBarLength: Int = 50,
    val percentageDecimals: Int = 2,
)
