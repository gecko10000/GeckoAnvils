@file:Suppress("UnstableApiUsage")

package gecko10000.geckoanvils

import gecko10000.geckoanvils.config.BootstrapConfig
import gecko10000.geckolib.config.YamlFileManager
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.keys.EnchantmentKeys
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import io.papermc.paper.tag.TagEntry

class MendingRemovalBootstrap : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        val config = YamlFileManager(
            configDirectory = context.dataDirectory.toFile(),
            configName = "bootstrap-config.yml",
            initialValue = BootstrapConfig(),
            serializer = BootstrapConfig.serializer()
        ).value
        if (!config.removeMending) return
        context.logger.info("Removing mending...")
        context.lifecycleManager.registerEventHandler(
            LifecycleEvents.TAGS.preFlatten(RegistryKey.ENCHANTMENT).newHandler { event ->
                val tag = event.registrar().getTag(EnchantmentTagKeys.TREASURE)
                val tagWithoutMending = tag.minus(TagEntry.valueEntry(EnchantmentKeys.MENDING))
                event.registrar().setTag(EnchantmentTagKeys.TREASURE, tagWithoutMending)
            })
    }

}
