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
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.view.AnvilView
import org.koin.core.component.inject
import redempt.redlib.misc.EventListener

@Suppress("UnstableApiUsage")
class AnvilRenameGUI(private val player: Player, block: Block) : InventoryHolder, MyKoinComponent {

    private val plugin: BetterAnvils by inject()
    private val shouldOpen: Boolean = Tag.ANVIL.isTagged(block.type)
    private val view: AnvilView = MenuType.ANVIL.create(player, Component.text("test"))
    private val safeMiniMessage: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolvers(
                    StandardTags.color(),
                    StandardTags.decorations(),
                    StandardTags.gradient(),
                )
                .build()
        )
        .build()

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
        }
        listeners += EventListener(PrepareAnvilEvent::class.java) { e ->
            val newName = view.renameText
            if (newName == null) {
                view.repairCost = plugin.config.simpleRenameCost
                return@EventListener
            }
            val result = e.result ?: return@EventListener
            val deserialized = safeMiniMessage.deserialize(newName)
            val isSimple = deserialized == Component.text(newName)
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
