package gecko10000.geckoanvils.guis

import gecko10000.geckoanvils.GeckoAnvils
import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckoanvils.managers.AnvilBlockManager
import gecko10000.geckolib.extensions.isEmpty
import gecko10000.geckolib.extensions.name
import gecko10000.geckolib.extensions.withDefaults
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.view.AnvilView
import org.koin.core.component.inject
import redempt.redlib.misc.EventListener
import redempt.redlib.misc.Task

@Suppress("UnstableApiUsage")
class AnvilRenameGUI(private val player: Player, private val block: Block) : InventoryHolder, MyKoinComponent {

    private val plugin: GeckoAnvils by inject()
    private val anvilBlockManager: AnvilBlockManager by inject()
    private val shouldOpen: Boolean = anvilBlockManager.isValid(block)
    private val view: AnvilView = MenuType.ANVIL.create(player, plugin.config.renameGUIName)
    private val selfDestructTagResolver: TagResolver = run {
        val methods = StandardTags::class.java.declaredMethods
        val extraResolvers = methods.filter { it.name in plugin.config.selfDestructTags }
            .filter { it.parameterCount == 0 }
            .map { it.invoke(null) }
            .filterIsInstance<TagResolver>()
        TagResolver.builder().resolvers(extraResolvers).build()
    }
    private val safeMiniMessage: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolvers(
                    StandardTags.color(),
                    StandardTags.decorations(),
                    StandardTags.gradient(),
                    StandardTags.shadowColor(),
                ).resolver(selfDestructTagResolver).build()
        ).build()
    private val selfDestructMiniMessage: MiniMessage = MiniMessage.builder().tags(selfDestructTagResolver).build()

    private fun start() {
        if (shouldOpen) {
            player.openInventory(view)
        } else {
            return
        }
        val listeners = mutableListOf<EventListener<*>>()
        listeners += EventListener(InventoryClickEvent::class.java) { e ->
            if (e.isCancelled) return@EventListener
            if (e.clickedInventory == null || e.clickedInventory != inventory) return@EventListener
            // prevent items in the second slot
            if (e.slot == 1) {
                e.isCancelled = true
                return@EventListener
            }
            if (e.slot != 2 || inventory.getItem(2).isEmpty()) return@EventListener
            if (!anvilBlockManager.isValid(block)) {
                // Prevent rename if anvil missing
                e.isCancelled = true
                player.closeInventory()
            } else {
                // Seems like the sound plays in the virtual
                // GUI, so we don't play it ourselves. A bit
                // inconsistent since it doesn't play the anvil
                // break sound, but close enough.
                anvilBlockManager.damageAnvil(block, playSound = false)
            }
            val newName = view.renameText ?: return@EventListener
            val simple = Component.text(newName)
            val blacklisted = simple != selfDestructMiniMessage.deserialize(newName)
            if (!blacklisted) return@EventListener
            e.isCancelled = true
            val item = inventory.getItem(2)?.clone() ?: return@EventListener
            inventory.setItem(0, null)
            player.closeInventory()
            val entity = player.world.dropItem(player.location.add(player.facing.direction).add(0.0, 0.1, 0.0), item)
            entity.isCustomNameVisible = true
            entity.customName(item.name())
            entity.setCanMobPickup(false)
            entity.setCanPlayerPickup(false)
            entity.isInvulnerable = true
            entity.fireTicks = 5 * 20
            val listener = EventListener(InventoryPickupItemEvent::class.java) { e ->
                if (e.item == entity) e.isCancelled = true
            }
            Task.syncDelayed({ ->
                entity.remove()
                listener.unregister()
            }, entity.fireTicks.toLong())
        }
        listeners += EventListener(PrepareAnvilEvent::class.java) { e ->
            val newName = view.renameText
            if (newName == null) {
                view.repairCost = plugin.config.simpleRenameCost
                return@EventListener
            }
            val result = e.result ?: return@EventListener
            val deserialized = safeMiniMessage.deserialize(newName)
            val simple = Component.text(newName)
            val isSimple = deserialized == simple
            result.editMeta {
                it.displayName(deserialized.withDefaults())
            }
            view.repairCost = if (isSimple) plugin.config.simpleRenameCost else plugin.config.coloredRenameCost
        }
        EventListener(InventoryCloseEvent::class.java) { l, e ->
            if (e.inventory == inventory) {
                listeners.forEach { it.unregister() }
                l.unregister()
            }
        }
    }

    init {
        start()
    }

    override fun getInventory(): Inventory {
        return view.topInventory
    }
}
