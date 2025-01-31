package gecko10000.geckoanvils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object DurationUtils {

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

}
