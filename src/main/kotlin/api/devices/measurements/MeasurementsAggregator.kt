package api.devices.measurements

import database.measurements.TimeAndValue

class MeasurementsAggregator {

        fun aggregate(
            intervals: List<Interval>,
            measurements: List<TimeAndValue>,
            sensor: Sensor,
        ): ArrayList<TimeAndValue> {
            val result: ArrayList<TimeAndValue> = arrayListOf()

            for (interval in intervals) {
                val filtered = measurementsInsideInterval(interval, measurements)
                if (filtered.bucket.isEmpty()) {
                    continue
                }

                if (sensor == Sensor.RAIN) {
                    val sum = sumOfInterval(filtered)
                    result.add(TimeAndValue(interval.until, sum))
                }
                else {
                    val avg = averageOfInterval(filtered)
                    result.add(TimeAndValue(interval.until, avg))
                }
            }

            return result
        }

    private fun measurementsInsideInterval(
        intervalParam: Interval,
        measurements: List<TimeAndValue>,
        ): Interval {
        val result = Interval(intervalParam.since, intervalParam.until, intervalParam.bucket)
        for (m in measurements) {
            if (intervalContainsMeasurement(m, intervalParam)) {
                result.bucket.add(TimeAndValue(m.timestamp, m.value))
            }
        }

        return result
    }

    private fun intervalContainsMeasurement(
        measurement: TimeAndValue,
        interval: Interval,
        ): Boolean {
        if (measurement.timestamp.isAfter(interval.until)) {
            return false
        }
        if (measurement.timestamp.isBefore(interval.since)) {
            return false
        }
        if (measurement.timestamp == interval.until) {
            return false
        }

        return true
    }

    private fun sumOfInterval(interval: Interval): Double {
        val sum = interval.bucket.sumOf { it.value }
        return sum
    }

    private fun averageOfInterval(interval: Interval): Double {
        val avg = interval.bucket
            .map { it.value }
            .average()
        return avg
    }

}
