package api.devices.measurements

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import database.Device
import database.DeviceDao
import database.Hold
import database.HoldDao
import database.measurements.TimeAndValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals

class GetMeasurementsTest {

    private lateinit var deviceDao: DeviceDao
    private lateinit var holdDao: HoldDao
    private lateinit var combiner: MeasurementHoldCombiner
    private lateinit var creator: IntervalCreator
    private lateinit var aggregator: MeasurementAggregator
    private lateinit var subject: GetMeasurements

    @BeforeEach
    fun setup() {
        deviceDao = mock()
        holdDao = mock()
        combiner = mock()
        creator = mock()
        aggregator = mock()

        subject = GetMeasurements(deviceDao, holdDao, combiner, creator, aggregator)
    }

    @ParameterizedTest
    @CsvSource(
        "airTemp",
        "soilTemp",
        "earth_temp",
        "wind_strength"
    )
    fun `return status 400 if the sensor input is invalid`(
        sensor: String,
    ) {
        val id = "device id"
        val since = "2022-12-14T15:00:00Z"
        val until = "2022-12-15T15:00:00Z"
        val resolution = 60L

        val res = subject.get(id, sensor, since, until, resolution)

        val expected = 400
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 400 if the since input is not formatted as an Instant`() {
        val id = "device id"
        val sensor = "rain"
        val since = "2022-12-14T15:00:00"
        val until = "2022-12-15T15:00:00Z"
        val resolution = 60L

        val res = subject.get(id, sensor, since, until, resolution)

        val expected = 400
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 400 if the until input is not formatted as an Instant`() {
        val id = "device id"
        val sensor = "rain"
        val since = "2022-12-14T15:00:00Z"
        val until = "2022-12-15T15:00:00"
        val resolution = 60L

        val res = subject.get(id, sensor, since, until, resolution)

        val expected = 400
        assertEquals(expected, res.status)
    }

    @ParameterizedTest
    @CsvSource(
        "2022-12-14T14:55:00Z",
        "2022-12-14T14:50:00Z",
    )
    fun `return status 422 if the until input is not over 10 minutes after since`(
        since: String,
    ) {
        val id = "device id"
        val sensor = "rain"
        val until = "2022-12-14T15:00:00Z"
        val resolution = 60L

        val res = subject.get(id, sensor, since, until, resolution)

        val expected = 422
        assertEquals(expected, res.status)
    }

    @ParameterizedTest
    @CsvSource(
        "9",
        "0",
        "-11",
    )
    fun `return status 422 if the resolution input is less than 10`(
        resolution: Long,
    ) {
        val id = "device id"
        val sensor = "rain"
        val since = "2022-12-14T15:00:00Z"
        val until = "2022-12-15T15:00:00Z"

        val res = subject.get(id, sensor, since, until, resolution)

        val expected = 422
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 422 if the resolution input is more than the time between since and until`() {
        val id = "device id"
        val sensor = "rain"
        val since = "2022-12-14T15:00:00Z"
        val until = "2022-12-14T16:00:00Z"
        val resolution = 61L

        val res = subject.get(id, sensor, since, until, resolution)

        val expected = 422
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 400 if the request is too big`() {
        val id = "device id"
        val sensor = "rain"
        val since = "2022-12-01T15:00:00Z"
        val until = "2022-12-15T15:00:00Z"
        val resolution = 10L

        val res = subject.get(id, sensor, since, until, resolution)

        val expected = 400
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 404 if the device has no holds`() {
        val id = "device id"
        val sensor = "rain"
        val since = "2022-12-14T15:00:00Z"
        val until = "2022-12-15T15:00:00Z"
        val resolution = 60L
        val device = Device("device id", "account id")
        val holds: List<Hold> = listOf()
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        val res = subject.get(id, sensor, since, until, resolution)

        val expected = 404
        assertEquals(expected, res.status)
    }

    @Test
    fun `call hold combiner with the correct input if the request is valid`() {
        val id = "device id"
        val sensorString = "rain"
        val sinceString = "2022-12-14T15:00:00Z"
        val untilString = "2022-12-15T15:00:00Z"
        val resolution = 60L
        val device = Device("device id", "account id")
        val hold = Hold(1, "device id", "label", "imei", Timestamp.valueOf("2023-01-01 12:00:00"), null)
        val holds: List<Hold> = listOf(hold)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2022-12-14T15:00:00Z")
        val until = Instant.parse("2022-12-15T15:00:00Z")
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        subject.get(id, sensorString, sinceString, untilString, resolution)

        verify(combiner).combine(holds, sensor, since, until)
    }

    @Test
    fun `call interval creator with the correct input if the request is valid`() {
        val id = "device id"
        val sensorString = "rain"
        val sinceString = "2022-12-14T15:00:00Z"
        val untilString = "2022-12-15T15:00:00Z"
        val resolutionLong = 60L
        val device = Device("device id", "account id")
        val hold = Hold(1, "device id", "label", "imei", Timestamp.valueOf("2023-01-01 12:00:00"), null)
        val holds: List<Hold> = listOf(hold)
        val resolution = Duration.ofMinutes(resolutionLong)
        val since = Instant.parse("2022-12-14T15:00:00Z")
        val until = Instant.parse("2022-12-15T15:00:00Z")
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        subject.get(id, sensorString, sinceString, untilString, resolutionLong)

        verify(creator).createIntervals(resolution, since, until)
    }

    @Test
    fun `call measurement aggregator with the correct input if the request is valid`() {
        val id = "device id"
        val sensorString = "rain"
        val sinceString = "2022-12-14T15:00:00Z"
        val untilString = "2022-12-15T15:00:00Z"
        val resolutionLong = 60L
        val device = Device("device id", "account id")
        val hold = Hold(1, "device id", "label", "imei", Timestamp.valueOf("2023-01-01 12:00:00"), null)
        val holds: List<Hold> = listOf(hold)
        val measurements: List<TimeAndValue> = listOf()
        val intervals: List<Interval> = listOf()
        val sensor = Sensor.RAIN
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)
        whenever(combiner.combine(any(), any(), any(), any())).thenReturn(measurements)
        whenever(creator.createIntervals(any(), any(), any())).thenReturn(intervals)

        subject.get(id, sensorString, sinceString, untilString, resolutionLong)

        verify(aggregator).aggregate(intervals, measurements, sensor)
    }

    @Test
    fun `return status 200 if the request is successful`() {
        val id = "device id"
        val sensor = "rain"
        val since = "2022-12-14T15:00:00Z"
        val until = "2022-12-15T15:00:00Z"
        val resolution = 60L
        val device = Device("device id", "account id")
        val hold = Hold(1, "device id", "label", "imei", Timestamp.valueOf("2023-01-01 12:00:00"), null)
        val holds: List<Hold> = listOf(hold)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        val res = subject.get(id, sensor, since, until, resolution)

        val expected = 200
        assertEquals(expected, res.status)
    }

}
