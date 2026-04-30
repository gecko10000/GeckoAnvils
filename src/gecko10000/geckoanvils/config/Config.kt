@file:UseSerializers(
    MMComponentSerializer::class,
    DurationSerializer::class,
)

package gecko10000.geckoanvils.config

import com.charleskorn.kaml.YamlComment
import gecko10000.geckolib.config.serializers.DurationSerializer
import gecko10000.geckolib.config.serializers.InternalItemStackSerializer
import gecko10000.geckolib.config.serializers.MMComponentSerializer
import gecko10000.geckolib.extensions.MM
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val itemRepairMultipliers = mapOf(
    "_AXE" to 3,
    "_PICKAXE" to 3,
    "_SHOVEL" to 1,
    "_HOE" to 2,
    "_SWORD" to 2,
    "_CHESTPLATE" to 8,
    "_HELMET" to 5,
    "_BOOTS" to 4,
    "_LEGGINGS" to 7,
)

private fun isTool(type: String) = type.endsWith("_AXE")
        || type.endsWith("_PICKAXE")
        || type.endsWith("_SHOVEL")
        || type.endsWith("_HOE")
        || type.endsWith("_SWORD")

// These are amounts for repairing with sticks, 2x for leather.
private val tierBaseAmounts = mapOf(
    // Default durability: 59
    "WOODEN" to 2,
    // Default durability: 131
    "STONE" to 5,
    // Durability similar to stone
    "LEATHER" to 5,
    // Default durability: 250
    "IRON" to 22,
    // Durability same as iron
    "CHAINMAIL" to 22,
    // Default durability: 32
    "GOLD" to 1,
    // Default durability: 1561
    "DIAMOND" to 105,
    // Default durability: 2031
    "NETHERITE" to 125,
)

private fun defaultRepairAmountsFor(m: Material): Map<String, Int>? {
    val name = m.name
    val armorOrTool = "_" + name.substringAfterLast('_')
    val multiplier = itemRepairMultipliers[armorOrTool] ?: return null
    val usedMaterial = name.substringBefore('_')
    val baseAmount = tierBaseAmounts[usedMaterial] ?: return null

    val map = mutableMapOf<String, Int>()
    if (isTool(armorOrTool)) {
        // Add stick, resource, and crafted item
        map["STICK"] = baseAmount
        map[usedMaterial] = baseAmount * 3
        map[m.name] = baseAmount * 3 * multiplier
    } else {
        // Add leather, resource, and crafted item
        map["LEATHER"] = baseAmount
        // This will override leather for leather armor, which is fine.
        map[usedMaterial] = baseAmount * 3
        map[m.name] = baseAmount * 3 * multiplier
    }
    return map
}

private fun defaultRepairAmounts(): Map<Material, Map<String, Int>> = buildMap {
    for (m in Material.entries) {
        val amounts = defaultRepairAmountsFor(m) ?: continue
        put(m, amounts)
    }
}

@Serializable
data class Config(
    val homeAnvilName: Component = MM.deserialize("<dark_gray>Anvil"),
    val enchantmentAnvilName: Component = MM.deserialize("<dark_purple>Enchantments"),
    val enchantmentStartName: Component = MM.deserialize("<#008B8B>Enchant, Disenchant, or Combine"),
    val itemRepairName: Component = MM.deserialize("<dark_green>Item Repair"),
    val repairStartName: Component = MM.deserialize("<#008B8B>Start Repair"),
    val renameGUIName: Component = MM.deserialize("<gradient:green:yellow>Rename Item"),
    val simpleRenameCost: Int = 15,
    val coloredRenameCost: Int = 30,
    @YamlComment(
        "Attempted renaming with these MM",
        "tags will be burned with fire."
    )
    val selfDestructTags: List<String> = listOf("clickEvent"),
    val baseEnchantmentCost: Int = 100,
    val baseEnchantmentDuration: Duration = 10.minutes,
    val baseDisenchantLevelCost: Int = 3,
    val baseDisenchantDuration: Duration = 20.minutes,
    val progressBarLength: Int = 50,
    val percentageDecimals: Int = 2,
    @Serializable(InternalItemStackSerializer::class)
    val maxDurabilityUpgradeItem: ItemStack = ItemStack.of(Material.BARRIER),
    val repairAmounts: Map<Material, Map<String, Int>> = defaultRepairAmounts(),
)
