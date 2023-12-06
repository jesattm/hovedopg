package api.devices.measurements

import database.measurements.TimeAndValue
import java.time.Duration
import java.time.Instant

class IntervalCreator {

    fun createIntervals(
        resolution: Duration,
        since: Instant,
        until: Instant,
    ): List<Interval> {
        val intervals: ArrayList<Interval> = arrayListOf()
        var current: Instant = since
        while (current.isBefore(until)) {
            val interval = Interval(current, current.plus(resolution))
            intervals.add(interval)
            current = current.plus(resolution)
        }

        return intervals
    }

}

data class Interval(
    val since: Instant,
    val until: Instant,
    val bucket: ArrayList<TimeAndValue> = arrayListOf(),
)
