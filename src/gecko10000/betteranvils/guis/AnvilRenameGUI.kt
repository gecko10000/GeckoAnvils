package gecko10000.betteranvils.guis

import gecko10000.betteranvils.BetterAnvils
import gecko10000.betteranvils.di.MyKoinComponent
import gecko10000.geckolib.extensions.withDefaults
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.Tag
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
class AnvilRenameGUI(private val player: Player, block: Block) : InventoryHolder, MyKoinComponent {

    private val plugin: BetterAnvils by inject()
    private val shouldOpen: Boolean = Tag.ANVIL.isTagged(block.type)
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
            if (e.slot != 2) return@EventListener
            val newName = view.renameText ?: return@EventListener
            val simple = Component.text(newName)
            val blacklisted = simple != selfDestructMiniMessage.deserialize(newName)
            if (!blacklisted) return@EventListener
            e.isCancelled = true
            val item = inventory.getItem(2)?.clone() ?: return@EventListener
            inventory.setItem(0, null)
            player.closeInventory()
            val entity = player.world.dropItem(player.location.add(player.facing.direction).add(0.0, 0.1, 0.0), item)
            entity.pickupDelay = 500
            entity.isInvulnerable = true
            entity.fireTicks = 50
            val listener = EventListener(InventoryPickupItemEvent::class.java) { e ->
                if (e.item == entity) e.isCancelled = true
            }
            Task.syncDelayed({ ->
                entity.remove()
                listener.unregister()
            }, 50L)
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
