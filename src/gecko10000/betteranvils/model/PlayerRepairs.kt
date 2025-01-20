package gecko10000.betteranvils.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerRepairs(
    val currentRepairs: List<RepairInfo?>,
)
