package database.measurements

import api.devices.measurements.Sensor
import java.time.Duration
import java.time.Instant
import java.util.Random

class FakeMeasurementsDatabase {

    fun query(
        imei: String,
        sensor: Sensor,
        since: Instant,
        until: Instant,
    ): List<TimeAndValue> {
        val seed = imei.hashCode().toLong()
        val random = Random(seed)

        val result = ArrayList<TimeAndValue>()
        var current = since

        while (current < until) {
            val timestamp = current
            val resolution = Duration.ofMinutes(10)
            current = current.plus(resolution)

            val value = when (sensor) {
                Sensor.AIR_TEMP -> random.nextDouble(-10.0, 30.0)
                Sensor.HUMIDITY -> random.nextDouble(25.0, 95.0)
                Sensor.PRESSURE -> random.nextDouble(900.0, 1100.0)
                Sensor.SOIL_TEMP -> random.nextDouble(-5.0, 25.0)
                Sensor.RAIN -> random.nextDouble(10.0) / 9
                Sensor.WIND -> random.nextDouble(0.0, 30.0)
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
