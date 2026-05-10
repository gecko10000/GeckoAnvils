package gecko10000.geckoanvils.config

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable

@Serializable
data class BootstrapConfig(
    @YamlComment("The values here require a full restart.")
    val removeMending: Boolean = true,
    val removeUnbreaking: Boolean = true,
)
