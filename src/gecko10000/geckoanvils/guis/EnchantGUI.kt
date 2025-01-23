package gecko10000.geckoanvils.guis

import gecko10000.geckoanvils.DurationFormatter
import gecko10000.geckoanvils.GeckoAnvils
import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckoanvils.managers.DataManager
import gecko10000.geckoanvils.managers.PermissionManager
import gecko10000.geckoanvils.model.EnchantInfo
import gecko10000.geckolib.extensions.MM
import gecko10000.geckolib.extensions.parseMM
import gecko10000.geckolib.extensions.withDefaults
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import redempt.redlib.inventorygui.InventoryGUI
import redempt.redlib.inventorygui.ItemButton
import redempt.redlib.itemutils.ItemUtils
import redempt.redlib.misc.Task
import kotlin.math.max
import kotlin.math.min
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class EnchantGUI(player: Player, block: Block) : MyKoinComponent, AnvilAssociatedGUI(player, block) {

    private val plugin: GeckoAnvils by inject()
    private val dataManager: DataManager by inject()
    private val permissionManager: PermissionManager by inject()

    private val data = dataManager.getData(player)
    private val allowedEnchants = permissionManager.getAllowedEnchants(player)
    private val missingEnchants = max(0, allowedEnchants - data.currentEnchants.size)
    private val concurrentEnchants = data.currentEnchants.plus(List(missingEnchants) { null })
    private val job: Job

    init {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                Task.syncDelayed { -> updateInventory() }
                delay(1.toDuration(DurationUnit.SECONDS))
            }
        }
    }

    private fun newEnchantButton(index: Int): ItemButton {
        val item = ItemStack.of(Material.LIME_STAINED_GLASS_PANE)
        item.editMeta {
            it.displayName(parseMM("<gray>(<green>+</green>) <dark_green>Start Enchantment"))
        }
        return ItemButton.create(item) { _ -> EnchantStartGUI(player, block, index) }
    }

    private fun remainingTime(data: EnchantInfo): Long {
        val currentTime = System.currentTimeMillis()
        val passedTime = currentTime - data.startTime
        return data.duration - passedTime
    }

    private fun timeString(remainingTime: Long): Component {
        if (remainingTime <= 0) {
            return parseMM("<green>Done! Click to collect.")
        } else {
            val duration = remainingTime.toDuration(DurationUnit.MILLISECONDS)
            return MM.deserialize(
                "<yellow><time>",
                Placeholder.unparsed("time", DurationFormatter.format(duration))
            ).withDefaults()
        }
    }

    private fun currentEnchantItem(data: EnchantInfo): ItemButton {
        val displayItem = data.outputItem.clone()
        val remainingTime = remainingTime(data)
        displayItem.editMeta {
            it.lore(
                it.lore().orEmpty().plus(
                    listOf(
                        Component.empty(),
                        timeString(remainingTime),
                    )
                )
            )
        }
        return ItemButton.create(displayItem) { _ ->
            if (remainingTime > 0) return@create
            // TODO: collect
            println("Collected item ${data.outputItem}.")
        }
    }

    private fun updateInventory(gui: InventoryGUI = this.inventory) {
        for (i in concurrentEnchants.indices) {
            val infoInSlot = concurrentEnchants[i]
            val button = if (infoInSlot == null) newEnchantButton(i) else currentEnchantItem(infoInSlot)
            gui.addButton(i, button)
        }
    }

    override fun createInventory(): InventoryGUI {
        val size = min(54, ItemUtils.minimumChestSize(concurrentEnchants.size) + 9)
        val gui = InventoryGUI(Bukkit.createInventory(this, size, plugin.config.enchantmentAnvilName))
        gui.fill(0, size, FILLER)
        updateInventory(gui)
        gui.addButton(size - 9, BACK { AnvilHomeGUI(player, block) })
        gui.setOnDestroy { job.cancel() }
        return gui
    }
}
