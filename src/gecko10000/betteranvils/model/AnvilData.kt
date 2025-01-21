package gecko10000.betteranvils.model

import kotlinx.serialization.Serializable

@Serializable
data class AnvilData(
    val currentEnchants: List<EnchantInfo?>,
    val currentRepairs: List<RepairInfo?>,
)
