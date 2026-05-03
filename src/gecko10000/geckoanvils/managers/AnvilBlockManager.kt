package gecko10000.geckoanvils.managers

import gecko10000.geckoanvils.GeckoAnvils
import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckoanvils.guis.AnvilHomeGUI
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.koin.core.component.inject
import kotlin.random.Random

class AnvilBlockManager : Listener, MyKoinComponent {

    companion object {
        private const val DAMAGE_PROBABILITY = 0.125
    }

    private val plugin: GeckoAnvils by inject()

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun PlayerInteractEvent.onOpenAnvil() {
        if (useInteractedBlock() == Event.Result.DENY) return
        if (action != Action.RIGHT_CLICK_BLOCK) return
        val block = clickedBlock ?: return
        if (!Tag.ANVIL.isTagged(block.type)) return
        isCancelled = true
        AnvilHomeGUI(player, block)
    }

    fun isValid(block: Block) = Tag.ANVIL.isTagged(block.type)

    fun damageAnvil(block: Block, playSound: Boolean = true) {
        val willBeDamaged = Random.nextFloat() < DAMAGE_PROBABILITY
        val willBreak = willBeDamaged && block.type == Material.DAMAGED_ANVIL
        val newType = when (block.type) {
            Material.ANVIL -> Material.CHIPPED_ANVIL
            Material.CHIPPED_ANVIL -> Material.DAMAGED_ANVIL
            Material.DAMAGED_ANVIL -> Material.AIR
            else -> block.type
        }
        if (willBeDamaged) block.type = newType
        if (playSound) {
            val soundKey = "block.anvil." + if (willBreak) "destroy" else "use"
            val sound = Sound.sound(Key.key(soundKey), Sound.Source.BLOCK, 1f, 2f)
            val loc = block.location
            block.world.playSound(sound, loc.x + 0.5, loc.y + 0.5, loc.z + 0.5)
        }
    }

}
