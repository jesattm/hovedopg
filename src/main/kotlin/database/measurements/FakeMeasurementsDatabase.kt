package database.measurements

import java.time.Duration
import java.time.Instant
import java.util.Random

class FakeMeasurementsDatabase {

    fun query(
        imei: String,
        sensor: String,
        since: Instant,
        until: Instant,
    ): List<TimeAndValue> {
        val seed = imei.hashCode().toLong()
        val random = Random(seed)

        val result = ArrayList<TimeAndValue>()
        var current = since

        while (current < until) {
            val timestamp = current
            val resolution = Duration.ofMinutes(30)
            current = current.plus(resolution)

            val value = when (sensor) {
                "air_temp" -> random.nextDouble(-10.0, 30.0)
                "humidity" -> random.nextDouble(25.0, 95.0)
                "pressure" -> random.nextDouble(900.0, 1100.0)
                "soil_temp" -> random.nextDouble(-5.0, 25.0)
                "rain" -> random.nextDouble(10.0) / 9
                "wind" -> random.nextDouble(0.0, 30.0)
                else -> throw IllegalArgumentException("Unknown sensor: $sensor")
            }
            result.add(TimeAndValue(timestamp, value))
        }

        return result
    }

}

data class TimeAndValue(
    val timestamp: Instant,
    val value: Double,
)
