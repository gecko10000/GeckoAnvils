@file:Suppress("UnstableApiUsage")

package gecko10000.geckoanvils

import gecko10000.geckoanvils.config.BootstrapConfig
import gecko10000.geckolib.config.YamlFileManager
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.keys.EnchantmentKeys
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import io.papermc.paper.registry.tag.TagKey
import io.papermc.paper.tag.PreFlattenTagRegistrar
import io.papermc.paper.tag.TagEntry
import org.bukkit.enchantments.Enchantment

class MendingRemovalBootstrap : PluginBootstrap {

    private fun removeFrom(
        context: BootstrapContext,
        enchantmentTag: TagKey<Enchantment>,
        enchantment: TypedKey<Enchantment>
    ) {
        context.lifecycleManager.registerEventHandler(
            LifecycleEvents.TAGS.preFlatten(RegistryKey.ENCHANTMENT).newHandler(
                { event: ReloadableRegistrarEvent<PreFlattenTagRegistrar<Enchantment>> ->
                    val tag = event.registrar().getTag(enchantmentTag)
                    val withoutEnchant = tag.minus(TagEntry.valueEntry(enchantment))
                    event.registrar().setTag(enchantmentTag, withoutEnchant)
                }
            )
        )
    }

    override fun bootstrap(context: BootstrapContext) {
        val config = YamlFileManager(
            configDirectory = context.dataDirectory.toFile(),
            configName = "bootstrap-config.yml",
            initialValue = BootstrapConfig(),
            serializer = BootstrapConfig.serializer()
        ).value
        if (config.removeMending) {
            context.logger.info("Removing mending...")
            removeFrom(context, EnchantmentTagKeys.TREASURE, EnchantmentKeys.MENDING)
        }
        if (config.removeUnbreaking) {
            context.logger.info("Removing unbreaking...")
            removeFrom(context, EnchantmentTagKeys.NON_TREASURE, EnchantmentKeys.UNBREAKING)
        }
        if (config.removeMending || config.removeUnbreaking) {
            context.logger.info("Done.")
        }
    }

}
