package gecko10000.betteranvils.guis

import gecko10000.geckolib.GUI
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.entity.Player

abstract class AnvilAssociatedGUI(player: Player, val block: Block) : GUI(player) {

    init {
        shouldOpen = shouldOpen && isValid()
    }

    fun isValid(): Boolean = Tag.ANVIL.isTagged(block.type)

}
