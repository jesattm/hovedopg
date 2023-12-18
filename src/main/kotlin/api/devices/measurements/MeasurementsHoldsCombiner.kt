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
            if (holdInTimeframe(hold, since, until)) {
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

    private fun holdInTimeframe(hold: Hold, start: Instant, end: Instant): Boolean {
        val startInTimeframe = inTimeframe(hold.start.toInstant(), start, end)

        if (hold.end == null) {
            val endInTimeframe = inTimeframe(Instant.now(), start, end)
            return startInTimeframe || endInTimeframe
        }

        val endInTimeframe = inTimeframe(hold.end.toInstant(), start, end)
        return startInTimeframe || endInTimeframe
    }

    private fun inTimeframe(timeValue: Instant, start: Instant, end: Instant
    ): Boolean {
        val inTimeframe = (timeValue.isAfter(start) || timeValue == start)
            && (timeValue.isBefore(end) || timeValue == end)
        return inTimeframe
    }

}
