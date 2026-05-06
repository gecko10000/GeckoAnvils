package gecko10000.geckoanvils.config

import kotlinx.serialization.Serializable
import org.bukkit.Material

@Serializable
data class RegexMaterialPredicate(
    private val regex: String,
) {

    constructor(material: Material) : this("^${material.name}$")

    val materials: Set<Material>
        get() = getMaterials(regex)

    companion object {
        private val REGEX_REGISTRY = mutableMapOf<String, Set<Material>>()
        private fun getMaterials(regexString: String): Set<Material> {
            REGEX_REGISTRY[regexString]?.let { return it }
            val regex = Regex(regexString)
            val materials = Material.entries.filter { it.name.matches(regex) }.toSet()
            REGEX_REGISTRY[regexString] = materials
            return materials
        }

    }
}
