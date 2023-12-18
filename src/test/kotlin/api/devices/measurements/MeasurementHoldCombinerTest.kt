package api.devices.measurements

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import database.Hold
import database.measurements.FakeMeasurementsDatabase
import database.measurements.TimeAndValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Timestamp
import java.time.Instant
import kotlin.test.assertEquals

class MeasurementHoldCombinerTest {

    private lateinit var measurements: FakeMeasurementsDatabase
    private lateinit var subject: MeasurementsHoldsCombiner

    @BeforeEach
    fun setup() {
        measurements = mock()

        subject = MeasurementsHoldsCombiner(measurements)
    }

    @Test
    fun `call measurements database with the correct start and end`() {

        val hold1 = Hold(1, "device id", "label", "imei", Timestamp.from(Instant.parse("2023-01-01T12:00:00Z")), Timestamp.from(Instant.parse("2023-01-02T12:00:00Z")))
        val hold2 = Hold(2, "device id", "label", "imei", Timestamp.from(Instant.parse("2023-01-03T12:00:00Z")), Timestamp.from(Instant.parse("2023-01-04T12:00:00Z")))
        val holds = listOf(hold1, hold2)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2023-01-01T20:00:00Z")
        val until = Instant.parse("2023-01-03T20:00:00Z")

        subject.combine(holds, sensor, since, until)

        val param3Captor = argumentCaptor<Instant>()
        val param4Captor = argumentCaptor<Instant>()
        verify(measurements, times(2)).query(any(), any(), param3Captor.capture(), param4Captor.capture())

        val expectedSince1 = Instant.parse("2023-01-01T20:00:00Z")
        val expectedUntil1 = Instant.parse("2023-01-02T12:00:00Z")
        val expectedSince2 = Instant.parse("2023-01-03T12:00:00Z")
        val expectedUntil2 = Instant.parse("2023-01-03T20:00:00Z")
        assertEquals(expectedSince1, param3Captor.allValues[0])
        assertEquals(expectedUntil1, param4Captor.allValues[0])
        assertEquals(expectedSince2, param3Captor.allValues[1])
        assertEquals(expectedUntil2, param4Captor.allValues[1])
    }

    @Test
    fun `return the correct amount of measurements`() {
        val hold1 = Hold(1, "device id", "label", "imei", Timestamp.valueOf("2023-01-01 12:00:00"), Timestamp.valueOf("2023-01-02 12:00:00"))
        val hold2 = Hold(2, "device id", "label", "imei", Timestamp.valueOf("2023-01-03 12:00:00"), Timestamp.valueOf("2023-01-04 12:00:00"))
        val holds = listOf(hold1, hold2)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2023-01-01T00:00:00Z")
        val until = Instant.parse("2023-02-01T00:00:00Z")
        val timeAndValue = TimeAndValue(Instant.parse("2023-01-01T00:00:00Z"), 1.0)
        whenever(measurements.query(any(), any(), any(), any()))
            .thenReturn(List(5) { timeAndValue } )
            .thenReturn(List(3) { timeAndValue } )

        val res = subject.combine(holds, sensor, since, until)

        val expected = 8
        assertEquals(expected, res.size)
    }

}
