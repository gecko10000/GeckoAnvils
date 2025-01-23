package gecko10000.geckoanvils.model

import kotlinx.serialization.Serializable

@Serializable
data class AnvilData(
    val currentEnchants: List<EnchantInfo?>,
    val currentRepairs: List<RepairInfo?>,
)
