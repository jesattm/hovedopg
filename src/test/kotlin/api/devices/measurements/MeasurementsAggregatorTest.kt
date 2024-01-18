package api.devices.measurements

import database.measurements.TimeAndValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Instant
import kotlin.test.assertEquals

class MeasurementsAggregatorTest {

    private lateinit var subject: MeasurementsAggregator

    @BeforeEach
    fun setup() {
        subject = MeasurementsAggregator()
    }

    @Test
    fun `return the summed value of the first interval's measurements if the sensor type is rain`() {
        val sensor = Sensor.RAIN
        val interval = Interval(Instant.parse("2023-01-01T12:00:00Z"), Instant.parse("2023-01-02T12:00:00Z"))
        val intervals = listOf(interval)
        val measurement1 = TimeAndValue(Instant.parse("2023-01-01T20:00:00Z"), 8.1)
        val measurement2 = TimeAndValue(Instant.parse("2023-01-01T21:00:00Z"), 3.7)
        val measurements = listOf(measurement1, measurement2)

        val res = subject.aggregate(intervals, measurements, sensor)

        val expected = 11.8
        assertEquals(expected, res.first().value)
    }

    @ParameterizedTest
    @CsvSource(
        "air_temp",
        "humidity",
        "wind",
    )
    fun `return the average value of the first interval's measurements if the sensor type is not rain`(
        sensorString: String,
    ) {
        val sensor = Sensor.valueOf(sensorString.uppercase())
        val interval = Interval(Instant.parse("2023-01-01T12:00:00Z"), Instant.parse("2023-01-02T12:00:00Z"))
        val intervals = listOf(interval)
        val measurement1 = TimeAndValue(Instant.parse("2023-01-01T20:00:00Z"), 8.1)
        val measurement2 = TimeAndValue(Instant.parse("2023-01-01T21:00:00Z"), 3.7)
        val measurements = listOf(measurement1, measurement2)

        val res = subject.aggregate(intervals, measurements, sensor)

        val expected = (8.1 + 3.7) / 2
        assertEquals(expected, res.first().value)
    }

    @Test
    fun `filter the measurements correctly into the intervals`() {
        val sensor = Sensor.RAIN
        val interval1 = Interval(Instant.parse("2023-01-01T12:00:00Z"), Instant.parse("2023-01-01T13:00:00Z"))
        val interval2 = Interval(Instant.parse("2023-01-01T13:00:00Z"), Instant.parse("2023-01-01T14:00:00Z"))
        val intervals = listOf(interval1, interval2)
        val measurement1 = TimeAndValue(Instant.parse("2023-01-01T12:00:00Z"), 1.0)
        val measurement2 = TimeAndValue(Instant.parse("2023-01-01T12:30:00Z"), 3.0)
        val measurement3 = TimeAndValue(Instant.parse("2023-01-01T13:00:00Z"), 5.0)
        val measurement4 = TimeAndValue(Instant.parse("2023-01-01T13:30:00Z"), 7.0)
        val measurements = listOf(measurement1, measurement2, measurement3, measurement4)

        val res = subject.aggregate(intervals, measurements, sensor)

        val expectedAggregationValue1 = 4.0
        val expectedAggregationValue2 = 12.0
        assertEquals(expectedAggregationValue1, res[0].value)
        assertEquals(expectedAggregationValue2, res[1].value)
    }

}
