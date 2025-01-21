@file:UseSerializers(MMComponentSerializer::class)

package gecko10000.betteranvils.config

import com.charleskorn.kaml.YamlComment
import gecko10000.geckolib.config.serializers.MMComponentSerializer
import gecko10000.geckolib.extensions.MM
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.kyori.adventure.text.Component

@Serializable
data class Config(
    val homeAnvilName: Component = MM.deserialize("<dark_gray>Anvil"),
    val enchantmentAnvilName: Component = MM.deserialize("<dark_purple>Enchantments"),
    val enchantmentStartName: Component = MM.deserialize("<gold>Enchant Item or Combine Books"),
    val itemRepairName: Component = MM.deserialize("<dark_green>Item Repair"),
    val repairStartName: Component = MM.deserialize("<gold>Start Repair"),
    val renameGUIName: Component = MM.deserialize("<gradient:blue:dark_green>Item Rename"),
    val simpleRenameCost: Int = 15,
    val coloredRenameCost: Int = 30,
    @YamlComment(
        "Attempted renaming with these MM",
        "tags will be burned with fire."
    )
    val selfDestructTags: List<String> = listOf("clickEvent")
)
