package gecko10000.geckoanvils

import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckolib.extensions.MM
import gecko10000.geckolib.extensions.parseMM
import gecko10000.geckolib.extensions.withDefaults
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.inventory.ItemStack
import org.koin.core.component.inject
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object DurationUtils : MyKoinComponent {

    private val plugin: GeckoAnvils by inject()

    val ONE_SEC = 1.seconds

    fun format(duration: Duration): String {
        val durationString = StringBuilder()
        duration.toComponents { days, hours, minutes, seconds, _ ->
            if (days > 0) durationString.append("${days}d ")
            if (hours > 0) durationString.append("${hours}h ")
            if (minutes > 0) durationString.append("${minutes}m ")
            if (seconds > 0 || durationString.isEmpty()) durationString.append("${seconds}s")
        }
        return durationString.trimEnd().toString()
    }

    fun clockFormat(duration: Duration): String {
        val durationString = StringBuilder()
        duration.toComponents { days, hours, minutes, seconds, _ ->
            if (days > 0) durationString.append(String.format("%d:", days))
            if (hours > 0 || durationString.isNotEmpty()) durationString.append(String.format("%02d:", hours))
            durationString.append(String.format("%02d:", minutes))
            durationString.append(String.format("%02d", seconds))
        }
        return durationString.toString()
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

    private fun time(remainingTime: Long): Component {
        val duration = remainingTime.milliseconds.plus(ONE_SEC)
        return MM.deserialize(
            "<yellow><time>",
            Placeholder.unparsed("time", clockFormat(duration))
        ).withDefaults()
    }

    fun ItemStack.addProgressInfo(progress: Double, remainingTime: Long) {
        this.editMeta {
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
    }

}
