package gecko10000.betteranvils

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object DurationFormatter {

    fun format(duration: Duration): String {
        val duration = duration.plus(1.toDuration(DurationUnit.SECONDS))
        val durationString = StringBuilder()
        val days = duration.inWholeDays
        if (days > 0) durationString.append("${days}d ")
        val hours = duration.inWholeHours % 24
        if (hours > 0) durationString.append("${hours}h ")
        val minutes = duration.inWholeMinutes % 60
        if (minutes > 0) durationString.append("${minutes}m ")
        val seconds = duration.inWholeSeconds % 60
        durationString.append("${seconds}s")
        return durationString.toString()
    }
}
