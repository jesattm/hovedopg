package api.devices.measurements

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import database.Hold
import database.measurements.FakeMeasurementsDatabase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Timestamp
import java.time.Instant
import kotlin.test.assertEquals

class MeasurementsHoldsCombinerTest {

    private lateinit var measurements: FakeMeasurementsDatabase
    private lateinit var subject: MeasurementsHoldsCombiner

    @BeforeEach
    fun setup() {
        measurements = mock()

        subject = MeasurementsHoldsCombiner(measurements)
    }

    @Test
    fun `if a hold starts and ends before the given timeframe then do not call the measurements database`() {
        val hold1 = Hold(1, "device id", "label", "imei",
            Timestamp.valueOf("2023-01-01 12:00:00"), Timestamp.valueOf("2023-01-02 12:00:00"))
        val holds = listOf(hold1)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2023-01-03T12:00:00Z")
        val until = Instant.parse("2023-01-05T12:00:00Z")

        subject.combine(holds, sensor, since, until)

        verify(measurements, times(0)).query(any(), any(), any(), any())
    }

    @Test
    fun `if a hold starts and ends after the given timeframe then do not call the measurements database`() {
        val hold1 = Hold(1, "device id", "label", "imei",
            Timestamp.valueOf("2023-01-06 12:00:00"), Timestamp.valueOf("2023-01-07 12:00:00"))
        val holds = listOf(hold1)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2023-01-03T12:00:00Z")
        val until = Instant.parse("2023-01-05T12:00:00Z")

        subject.combine(holds, sensor, since, until)

        verify(measurements, times(0)).query(any(), any(), any(), any())
    }

    @Test
    fun `if a hold starts outside of but ends inside of the given timeframe then call the measurements database`() {
        val hold1 = Hold(1, "device id", "label", "imei",
            Timestamp.valueOf("2023-01-01 12:00:00"), Timestamp.valueOf("2023-01-04 12:00:00"))
        val holds = listOf(hold1)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2023-01-03T12:00:00Z")
        val until = Instant.parse("2023-01-05T12:00:00Z")

        subject.combine(holds, sensor, since, until)

        verify(measurements).query(any(), any(), any(), any())
    }

    @Test
    fun `if a hold starts and ends inside of the given timeframe then call the measurements database`() {
        val hold1 = Hold(1, "device id", "label", "imei",
            Timestamp.valueOf("2023-01-03 13:00:00"), Timestamp.valueOf("2023-01-04 12:00:00"))
        val holds = listOf(hold1)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2023-01-03T12:00:00Z")
        val until = Instant.parse("2023-01-05T12:00:00Z")

        subject.combine(holds, sensor, since, until)

        verify(measurements).query(any(), any(), any(), any())
    }

    @Test
    fun `if a hold starts inside but ends outside of the given timeframe then call the measurements database`() {
        val hold1 = Hold(1, "device id", "label", "imei",
            Timestamp.valueOf("2023-01-03 13:00:00"), Timestamp.valueOf("2023-01-06 12:00:00"))
        val holds = listOf(hold1)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2023-01-03T12:00:00Z")
        val until = Instant.parse("2023-01-05T12:00:00Z")

        subject.combine(holds, sensor, since, until)

        verify(measurements).query(any(), any(), any(), any())
    }

    @Test
    fun `if a hold starts and ends outside of the given timeframe then call the measurements database`() {
        val hold1 = Hold(1, "device id", "label", "imei",
            Timestamp.valueOf("2023-01-02 12:00:00"), Timestamp.valueOf("2023-01-06 12:00:00"))
        val holds = listOf(hold1)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2023-01-03T12:00:00Z")
        val until = Instant.parse("2023-01-05T12:00:00Z")

        subject.combine(holds, sensor, since, until)

        verify(measurements).query(any(), any(), any(), any())
    }

    @Test
    fun `if a hold starts before the since value then call the measurements database with the since value`() {
        val hold1 = Hold(1, "device id", "label", "imei",
            Timestamp.from(Instant.parse("2023-01-01T12:00:00Z")),
            Timestamp.from(Instant.parse("2023-01-02T12:00:00Z")))
        val holds = listOf(hold1)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2023-01-01T20:00:00Z")
        val until = Instant.parse("2023-01-03T20:00:00Z")

        subject.combine(holds, sensor, since, until)

        val param3Captor = argumentCaptor<Instant>()
        verify(measurements, times(1)).query(any(), any(), param3Captor.capture(), any())

        val expected = Instant.parse("2023-01-01T20:00:00Z")
        assertEquals(expected, param3Captor.allValues[0])
    }

    @Test
    fun `if a hold starts after the since value then call the measurements database with the hold's start value`() {
        val hold1 = Hold(1, "device id", "label", "imei",
            Timestamp.from(Instant.parse("2023-01-01T21:00:00Z")),
            Timestamp.from(Instant.parse("2023-01-02T12:00:00Z")))
        val holds = listOf(hold1)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2023-01-01T20:00:00Z")
        val until = Instant.parse("2023-01-03T20:00:00Z")

        subject.combine(holds, sensor, since, until)

        val param3Captor = argumentCaptor<Instant>()
        verify(measurements, times(1)).query(any(), any(), param3Captor.capture(), any())

        val expected = Instant.parse("2023-01-01T21:00:00Z")
        assertEquals(expected, param3Captor.allValues[0])
    }
    @Test
    fun `if a hold starts at the same time as the since value then call the measurements database with the hold's start value`() {
        val hold1 = Hold(1, "device id", "label", "imei",
            Timestamp.from(Instant.parse("2023-01-01T20:00:00Z")),
            Timestamp.from(Instant.parse("2023-01-02T12:00:00Z")))
        val holds = listOf(hold1)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2023-01-01T20:00:00Z")
        val until = Instant.parse("2023-01-03T20:00:00Z")

        subject.combine(holds, sensor, since, until)

        val param3Captor = argumentCaptor<Instant>()
        verify(measurements, times(1)).query(any(), any(), param3Captor.capture(), any())

        val expected = Instant.parse("2023-01-01T20:00:00Z")
        assertEquals(expected, param3Captor.allValues[0])
    }

    @Test
    fun `if a hold ends after the until value then call the measurements database with the until value`() {
        val hold1 = Hold(1, "device id", "label", "imei",
            Timestamp.from(Instant.parse("2023-01-01T20:00:00Z")),
            Timestamp.from(Instant.parse("2023-01-04T12:00:00Z")))
        val holds = listOf(hold1)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2023-01-01T20:00:00Z")
        val until = Instant.parse("2023-01-03T20:00:00Z")

        subject.combine(holds, sensor, since, until)

        val param4Captor = argumentCaptor<Instant>()
        verify(measurements, times(1)).query(any(), any(), any(), param4Captor.capture())

        val expected = Instant.parse("2023-01-03T20:00:00Z")
        assertEquals(expected, param4Captor.allValues[0])
    }

    @Test
    fun `if a hold ends before the until value then call the measurements database with hold's end value`() {
        val hold1 = Hold(1, "device id", "label", "imei",
            Timestamp.from(Instant.parse("2023-01-01T20:00:00Z")),
            Timestamp.from(Instant.parse("2023-01-03T12:00:00Z")))
        val holds = listOf(hold1)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2023-01-01T20:00:00Z")
        val until = Instant.parse("2023-01-03T20:00:00Z")

        subject.combine(holds, sensor, since, until)

        val param4Captor = argumentCaptor<Instant>()
        verify(measurements, times(1)).query(any(), any(), any(), param4Captor.capture())

        val expected = Instant.parse("2023-01-03T12:00:00Z")
        assertEquals(expected, param4Captor.allValues[0])
    }

    @Test
    fun `if a hold ends at the same time as the until value then call the measurements database with hold's end value`() {
        val hold1 = Hold(1, "device id", "label", "imei",
            Timestamp.from(Instant.parse("2023-01-01T20:00:00Z")),
            Timestamp.from(Instant.parse("2023-01-03T20:00:00Z")))
        val holds = listOf(hold1)
        val sensor = Sensor.RAIN
        val since = Instant.parse("2023-01-01T20:00:00Z")
        val until = Instant.parse("2023-01-03T20:00:00Z")

        subject.combine(holds, sensor, since, until)

        val param4Captor = argumentCaptor<Instant>()
        verify(measurements, times(1)).query(any(), any(), any(), param4Captor.capture())

        val expected = Instant.parse("2023-01-03T20:00:00Z")
        assertEquals(expected, param4Captor.allValues[0])
    }

}
