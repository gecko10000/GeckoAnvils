package gecko10000.geckoanvils.guis

import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckoanvils.managers.AnvilBlockManager
import gecko10000.geckolib.GUI
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.koin.core.component.inject

abstract class AnvilAssociatedGUI(player: Player, val block: Block) : GUI(player), MyKoinComponent {

    private val anvilBlockManager: AnvilBlockManager by inject()

    init {
        shouldOpen = shouldOpen && anvilBlockManager.isValid(block)
    }

}
