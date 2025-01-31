package gecko10000.geckoanvils.guis

import gecko10000.geckoanvils.DurationUtils
import gecko10000.geckoanvils.GeckoAnvils
import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckoanvils.managers.DataManager
import gecko10000.geckoanvils.managers.PermissionManager
import gecko10000.geckoanvils.model.EnchantInfo
import gecko10000.geckolib.extensions.*
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class EnchantGUI(player: Player, block: Block) : MyKoinComponent, AnvilAssociatedGUI(player, block) {

    companion object {
        private val ONE_SEC = 1.seconds
    }

    private val plugin: GeckoAnvils by inject()
    private val dataManager: DataManager by inject()
    private val permissionManager: PermissionManager by inject()

    private val data = dataManager.getData(player)
    private val concurrentEnchants =
        data.currentEnchants.extend(targetAmount = permissionManager.getAllowedEnchants(player), element = null)
    private val job: Job

    init {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                Task.syncDelayed { -> updateInventory() }
                delay(DurationUtils.ONE_SEC)
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

    private fun time(remainingTime: Long): Component {
        val duration = remainingTime.milliseconds.plus(DurationUtils.ONE_SEC)
        return MM.deserialize(
            "<yellow><time>",
            Placeholder.unparsed("time", DurationUtils.clockFormat(duration))
        ).withDefaults()
    }

    private fun percentage(progress: Double): Component {
        val precision = plugin.config.percentageDecimals
        val pow = 10.0.pow(precision)
        val percent = progress * 100
        val roundedPercent = round(min(percent * pow, 100 * pow - 1)) / pow
        val formatted = String.format("%.${precision}f", roundedPercent)
        return parseMM("<yellow>$formatted%")
    }

    private fun progressBar(progress: Double): Component {
        val length = plugin.config.progressBarLength
        val filledCount = round(length * progress).toInt()
        return Component.text("|".repeat(filledCount), NamedTextColor.GREEN).append(
            Component.text("|".repeat(length - filledCount), NamedTextColor.RED)
        ).withDefaults()
    }

    private fun currentEnchantItem(index: Int, data: EnchantInfo): ItemButton {
        val displayItem = data.outputItem.clone()
        val passedTime = System.currentTimeMillis() - data.startTime
        val progress = passedTime.toDouble() / data.duration
        val remainingTime = data.duration - passedTime
        if (remainingTime <= 0) return doneEnchantButton(index, data)
        displayItem.editMeta {
            it.lore(
                it.lore().orEmpty().plus(
                    listOf(
                        Component.empty(),
                        percentage(progress).append(Component.text(" - ")).append(time(remainingTime)),
                        progressBar(progress),
                    )
                )
            )
        }
        return ItemButton.create(displayItem) { _ -> }
    }

    private fun doneEnchantButton(index: Int, data: EnchantInfo): ItemButton {
        val displayItem = data.outputItem.clone()
        displayItem.editMeta {
            it.lore(
                it.lore().orEmpty().plus(
                    listOf(
                        Component.empty(),
                        parseMM("<green>Done! Click to collect.")
                    )
                )
            )
        }
        return ItemButton.create(displayItem) { _ ->
            val item = data.outputItem.clone()
            val newEnchants = concurrentEnchants.updated(index) { null }
            val newData = this.data.copy(currentEnchants = newEnchants)
            dataManager.setData(player, newData)
            ItemUtils.give(player, item)
            EnchantGUI(player, block)
        }
    }

    private fun updateInventory(gui: InventoryGUI = this.inventory) {
        for (i in concurrentEnchants.indices) {
            val infoInSlot = concurrentEnchants[i]
            val button = if (infoInSlot == null) newEnchantButton(i) else currentEnchantItem(i, infoInSlot)
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
