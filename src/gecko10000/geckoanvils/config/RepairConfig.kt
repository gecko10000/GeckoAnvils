package gecko10000.geckoanvils.config

import kotlinx.serialization.Serializable
import org.bukkit.Material
import kotlin.time.Duration.Companion.minutes

@Serializable
data class RepairConfig(
    val repairs: Map<Material, List<RepairEntry>> = DEFAULT_REPAIR_AMOUNTS,
) {

    companion object {
        private val TOOL_MATERIAL_TYPES = setOf("WOODEN", "STONE", "COPPER", "IRON", "GOLDEN", "DIAMOND", "NETHERITE")
        private val TOOL_TYPES = setOf("AXE", "PICKAXE", "SHOVEL", "HOE", "SWORD", "SPEAR")
        private val ARMOR_MATERIAL_TYPES =
            setOf("LEATHER", "COPPER", "CHAINMAIL", "IRON", "GOLDEN", "DIAMOND", "NETHERITE")
        private val ARMOR_TYPES = setOf("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS")

        // https://minecraft.wiki/w/Durability
        private val DEFAULT_MATERIAL_MAPPINGS = mapOf<String, List<RepairEntry>>(
            // Default durability: 59
            "WOODEN" to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(".*_PLANKS"),
                    baseRepairAmount = 200,
                    maxDurabilityIncrease = 100,
                    time = 5.minutes,
                )
            ),
            // Durability similar to wood
            "LEATHER" to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.LEATHER),
                    baseRepairAmount = 250,
                    maxDurabilityIncrease = 200,
                    time = 5.minutes,
                )
            ),
            // Default durability: 131
            "STONE" to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.COBBLESTONE),
                    baseRepairAmount = 50,
                    maxDurabilityIncrease = 10,
                    time = 10.minutes,
                )
            ),
            // Default durability: 190
            "COPPER" to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.COPPER_INGOT),
                    baseRepairAmount = 75,
                    maxDurabilityIncrease = 6,
                    time = 10.minutes,
                )
            ),
            // Default durability: 250
            "IRON" to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.IRON_BLOCK),
                    maxDurabilityIncrease = 250,
                    time = 60.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.IRON_INGOT),
                    baseRepairAmount = 100,
                    time = 10.minutes,
                ),
            ),
            // Durability same as iron
            "CHAINMAIL" to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.IRON_INGOT),
                    baseRepairAmount = 90,
                    maxDurabilityIncrease = 8,
                    time = 15.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.IRON_CHAIN),
                    baseRepairAmount = 90,
                    maxDurabilityIncrease = 12,
                    time = 20.minutes,
                )
            ),
            // Default durability: 32
            "GOLDEN" to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.GOLD_INGOT),
                    baseRepairAmount = 20,
                    maxDurabilityIncrease = 15,
                    time = 10.minutes,
                )
            ),
            // Default durability: 1561
            "DIAMOND" to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.DIAMOND),
                    baseRepairAmount = 300,
                    time = 10.minutes,
                ),
            ),
            // Default durability: 2031
            "NETHERITE" to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.NETHERITE_INGOT),
                    maxDurabilityIncrease = 400,
                    time = 90.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.GOLD_BLOCK),
                    baseRepairAmount = 500,
                    time = 120.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.GOLD_INGOT),
                    baseRepairAmount = 100,
                    time = 15.minutes,
                ),
            ),
        )

        private val OTHER_DEFAULT_REPAIRS: Map<Material, List<RepairEntry>> = mapOf(
            // 275 durability
            Material.TURTLE_HELMET to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.TURTLE_SCUTE),
                    maxDurabilityIncrease = 40,
                    time = 40.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.SEAGRASS),
                    baseRepairAmount = 40,
                    time = 10.minutes,
                ),
            ),
            // 64
            Material.FLINT_AND_STEEL to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.FLINT),
                    baseRepairAmount = 40,
                    maxDurabilityIncrease = 8,
                    time = 15.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.IRON_INGOT),
                    baseRepairAmount = 50,
                    maxDurabilityIncrease = 4,
                    time = 15.minutes,
                )
            ),
            // 64
            Material.BRUSH to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.FEATHER),
                    baseRepairAmount = 30,
                    maxDurabilityIncrease = 8,
                    time = 15.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.COPPER_INGOT),
                    baseRepairAmount = 50,
                    maxDurabilityIncrease = 4,
                    time = 15.minutes,
                )
            ),
            // 64
            Material.FISHING_ROD to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.STRING),
                    maxDurabilityIncrease = 5,
                    time = 20.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.STICK),
                    baseRepairAmount = 10,
                    time = 5.minutes,
                )
            ),
            // 25
            Material.CARROT_ON_A_STICK to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.CARROT),
                    baseRepairAmount = 20,
                    maxDurabilityIncrease = 15,
                    time = 10.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.STRING),
                    baseRepairAmount = 15,
                    maxDurabilityIncrease = 5,
                    time = 5.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.STICK),
                    baseRepairAmount = 10,
                    maxDurabilityIncrease = 2,
                    time = 5.minutes,
                )
            ),
            // 100
            Material.WARPED_FUNGUS_ON_A_STICK to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.WARPED_FUNGUS),
                    baseRepairAmount = 75,
                    maxDurabilityIncrease = 30,
                    time = 25.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.STRING),
                    baseRepairAmount = 35,
                    maxDurabilityIncrease = 15,
                    time = 15.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.STICK),
                    baseRepairAmount = 20,
                    maxDurabilityIncrease = 5,
                    time = 10.minutes,
                )
            ),
            // 238
            Material.SHEARS to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.IRON_INGOT),
                    baseRepairAmount = 100,
                    maxDurabilityIncrease = 35,
                    time = 30.minutes,
                )
            ),
            // 336
            Material.SHIELD to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.IRON_INGOT),
                    maxDurabilityIncrease = 45,
                    time = 60.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(".*_PLANKS"),
                    baseRepairAmount = 30,
                    time = 10.minutes,
                ),
            ),
            // 384
            Material.BOW to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.STRING),
                    maxDurabilityIncrease = 25,
                    time = 25.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.STICK),
                    baseRepairAmount = 10,
                    time = 3.minutes,
                ),
            ),
            // 250
            Material.TRIDENT to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.HEART_OF_THE_SEA),
                    maxDurabilityIncrease = 120,
                    time = 120.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.NAUTILUS_SHELL),
                    baseRepairAmount = 50,
                    time = 20.minutes,
                ),
            ),
            // 432
            Material.ELYTRA to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.DRAGON_EGG),
                    maxDurabilityIncrease = 200,
                    time = 180.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.PHANTOM_MEMBRANE),
                    baseRepairAmount = 150,
                    time = 120.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.FEATHER),
                    baseRepairAmount = 30,
                    time = 30.minutes,
                ),
            ),
            // 465
            Material.CROSSBOW to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.IRON_INGOT),
                    maxDurabilityIncrease = 50,
                    time = 60.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.STRING),
                    baseRepairAmount = 60,
                    time = 45.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.STICK),
                    baseRepairAmount = 10,
                    time = 4.minutes,
                )
            ),
            // 500
            Material.MACE to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.HEAVY_CORE),
                    maxDurabilityIncrease = 100,
                    time = 90.minutes,
                ),
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.BREEZE_ROD),
                    baseRepairAmount = 75,
                    time = 25.minutes,
                )
            ),
            // 64
            Material.WOLF_ARMOR to listOf(
                RepairEntry(
                    matchedMaterials = RegexMaterialPredicate(Material.ARMADILLO_SCUTE),
                    baseRepairAmount = 20,
                    maxDurabilityIncrease = 5,
                    time = 10.minutes,
                )
            ),
        )

        private val DEFAULT_STICK_REPAIR_ENTRY = RepairEntry(
            matchedMaterials = RegexMaterialPredicate(Material.STICK),
            baseRepairAmount = 10,
            time = 2.minutes,
        )

        private val DEFAULT_LEATHER_REPAIR_ENTRY = RepairEntry(
            matchedMaterials = RegexMaterialPredicate(Material.LEATHER),
            baseRepairAmount = 15,
            time = 2.minutes,
        )

        private val DEFAULT_REPAIR_AMOUNTS: Map<Material, List<RepairEntry>> = buildMap {
            this.putAll(OTHER_DEFAULT_REPAIRS)
            for (materialType in TOOL_MATERIAL_TYPES) {
                for (toolType in TOOL_TYPES) {
                    val material = Material.getMaterial("${materialType}_${toolType}") ?: continue
                    val repairEntries = DEFAULT_MATERIAL_MAPPINGS.getValue(materialType)
                        .plus(DEFAULT_STICK_REPAIR_ENTRY)
                    this[material] = repairEntries
                }
            }
            for (materialType in ARMOR_MATERIAL_TYPES) {
                for (armorType in ARMOR_TYPES) {
                    val material = Material.getMaterial("${materialType}_${armorType}") ?: continue
                    val repairEntries = DEFAULT_MATERIAL_MAPPINGS.getValue(materialType).toMutableList()
                    // Do not add default leather entry to leather armor entries
                    if (repairEntries.none { it.matchedMaterials.materials.contains(Material.LEATHER) }) {
                        repairEntries += DEFAULT_LEATHER_REPAIR_ENTRY
                    }
                    this[material] = repairEntries
                }
            }
        }

    }
}
