package gecko10000.betteranvils.managers

import gecko10000.betteranvils.di.MyKoinComponent
import gecko10000.betteranvils.guis.AnvilHomeGUI
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import redempt.redlib.misc.EventListener
import kotlin.random.Random

class AnvilBlockManager : MyKoinComponent {

    companion object {
        private const val DAMAGE_PROBABILITY = 0.125
    }

    init {
        EventListener(PlayerInteractEvent::class.java, EventPriority.MONITOR) { e ->
            if (e.useInteractedBlock() == Event.Result.DENY) return@EventListener
            if (e.action != Action.RIGHT_CLICK_BLOCK) return@EventListener
            val block = e.clickedBlock ?: return@EventListener
            if (!Tag.ANVIL.isTagged(block.type)) return@EventListener
            e.isCancelled = true
            AnvilHomeGUI(e.player, block)
        }
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
            val sound = if (willBreak) Sound.BLOCK_ANVIL_BREAK else Sound.BLOCK_ANVIL_USE
            block.world.playSound(block.location.toCenterLocation(), sound, 1f, 1f)
        }
    }

}
