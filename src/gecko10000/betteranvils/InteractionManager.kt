package gecko10000.betteranvils

import gecko10000.betteranvils.di.MyKoinComponent
import gecko10000.betteranvils.guis.AnvilHomeGUI
import org.bukkit.Tag
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import redempt.redlib.misc.EventListener

class InteractionManager : MyKoinComponent {

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

}
