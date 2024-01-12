package api.devices.measurements

import database.Hold
import database.measurements.FakeMeasurementsDatabase
import database.measurements.TimeAndValue
import java.time.Instant

class MeasurementsHoldsCombiner(
    private val measurements: FakeMeasurementsDatabase,
    ) {

    fun combine(
        holds: List<Hold>,
        sensor: Sensor,
        since: Instant,
        until: Instant,
        ): List<TimeAndValue> {
        val measurementsList: ArrayList<TimeAndValue> = arrayListOf()
        val sorted = holds.sortedBy { it.start }
        for (hold in sorted) {
            if (periodsOverlap(hold, since, until)) {
                val start = findHoldStart(since, until, hold)
                val end = findHoldEnd(since, until, hold)

                val holdMeasurements = measurements.query(hold.imei, sensor, start, end)
                measurementsList.addAll(holdMeasurements)
            }
        }

        return measurementsList
    }

    private fun findHoldStart(since: Instant, until: Instant, hold: Hold): Instant {
        val start = hold.start.toInstant()
        if (!inTimeframe(start, since, until)) {
            return since
        }
        return start
    }

    private fun findHoldEnd(since: Instant, until: Instant, hold: Hold): Instant {
        val holdEnd: Instant
        if (hold.end == null) {
            holdEnd = Instant.now()
        }
        else {
            holdEnd = hold.end.toInstant()
        }

        if (!inTimeframe(holdEnd, since, until)) {
            return until
        }
        return holdEnd
    }

    private fun periodsOverlap(hold: Hold, timeframeStart: Instant, timeframeEnd: Instant): Boolean {
        val holdStart = hold.start.toInstant()
        val holdEnd: Instant
        if (hold.end == null) {
            holdEnd = Instant.now()
        } else {
            holdEnd = hold.end.toInstant()
        }

        val overlap = (holdStart <= timeframeEnd) && (holdEnd >= timeframeStart)
        return overlap
    }

    private fun inTimeframe(timeValue: Instant, start: Instant, end: Instant
    ): Boolean {
        val inTimeframe = (timeValue.isAfter(start) || timeValue == start)
            && (timeValue.isBefore(end) || timeValue == end)
        return inTimeframe
    }

}
