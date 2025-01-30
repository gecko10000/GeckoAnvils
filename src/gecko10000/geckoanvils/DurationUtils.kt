package gecko10000.geckoanvils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object DurationUtils {

    val ONE_SEC = 1.seconds

    fun format(duration: Duration): String {
        val durationString = StringBuilder()
        val days = duration.inWholeDays
        if (days > 0) durationString.append("${days}d ")
        val hours = duration.inWholeHours % 24
        if (hours > 0) durationString.append("${hours}h ")
        val minutes = duration.inWholeMinutes % 60
        if (minutes > 0) durationString.append("${minutes}m ")
        val seconds = duration.inWholeSeconds % 60
        if (seconds > 0 || durationString.isBlank()) durationString.append("${seconds}s")
        return durationString.trimEnd().toString()
    }
}
