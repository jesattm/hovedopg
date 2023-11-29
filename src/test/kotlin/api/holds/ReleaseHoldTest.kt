package api.holds

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import database.Device
import database.DeviceDao
import database.Hold
import database.HoldDao
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.sql.Timestamp
import java.time.Instant
import kotlin.test.assertEquals

class ReleaseHoldTest {

    private lateinit var deviceDao: DeviceDao
    private lateinit var holdDao: HoldDao
    private lateinit var finder: ActiveHoldFinder
    private lateinit var subject: ReleaseHold

    @BeforeEach
    fun setup() {
        deviceDao = mock()
        holdDao = mock()
        finder = mock()

        subject = ReleaseHold(deviceDao, holdDao, finder)
    }

    @Test
    fun `return status 404 if the device was not found in the database`() {
        val deviceId = "1"
        val body = ReleaseHoldBody(Instant.parse("2022-12-15T15:00:00Z"))
        whenever(deviceDao.findById(any())).thenReturn(null)

        val res = subject.release(deviceId, body)

        val expected = 404
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 404 if the device has no holds`() {
        val deviceId = "1"
        val body = ReleaseHoldBody(Instant.parse("2022-12-15T15:00:00Z"))
        val device = Device("1", "1")
        val holds: List<Hold> = listOf()
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        val res = subject.release(deviceId, body)

        val expected = 404
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 409 if there is no active hold on the device`() {
        val deviceId = "1"
        val body = ReleaseHoldBody(Instant.parse("2022-12-15T15:00:00Z"))
        val device = Device("1", "1")
        val start = Timestamp.from(Instant.parse("2022-12-15T15:00:00Z"))
        val end = Timestamp.from(Instant.parse("2022-12-16T15:00:00Z"))
        val hold = Hold(1, deviceId,"1", null, start, end)
        val holds = listOf(hold)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        val res = subject.release(deviceId, body)

        val expected = 409
        assertEquals(expected, res.status)
    }

    @ParameterizedTest
    @CsvSource(
        "2022-12-14T15:00:00Z",
        "2022-12-15T15:00:00Z",
    )
    fun `return status 422 if the end input is not after the hold's start`(
        endString: String
    ) {
        val deviceId = "1"
        val end = Instant.parse(endString)
        val body = ReleaseHoldBody(end)
        val device = Device("1", "1")
        val timestamp = Timestamp.from(Instant.parse("2022-12-15T15:00:00Z"))
        val inactiveHold = Hold(1, "1", "1", null, timestamp, timestamp)
        val activeHold = Hold(2, "1", "2",null, timestamp, null)
        val holds = listOf(inactiveHold, activeHold)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)
        whenever(finder.find(any())).thenReturn(activeHold)

        val res = subject.release(deviceId, body)

        val expected = 422
        assertEquals(expected, res.status)
    }

    @Test
    fun `set end attribute on the active hold with the input value if the request is valid`() {
        val deviceId = "1"
        val end = Instant.parse("2022-12-16T15:00:00Z")
        val body = ReleaseHoldBody(end)
        val device = Device("1", "1")
        val timestamp = Timestamp.from(Instant.parse("2022-12-15T15:00:00Z"))
        val inactiveHold = Hold(1, "1", "1", null, timestamp, timestamp)
        val activeHold = Hold(2, "1", "2", null, timestamp, null)
        val holds = listOf(inactiveHold, activeHold)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)
        whenever(finder.find(any())).thenReturn(activeHold)

        subject.release(deviceId, body)

        verify(holdDao).setEnd(activeHold.id, end)
    }

    @Test
    fun `return status 204 if the request is successful`() {
        val deviceId = "1"
        val body = ReleaseHoldBody(Instant.parse("2022-12-16T15:00:00Z"))
        val device = Device("1", "1")
        val timestamp = Timestamp.from(Instant.parse("2022-12-15T15:00:00Z"))
        val inactiveHold = Hold(1, "1", "1", null, timestamp, timestamp)
        val activeHold = Hold(2, "1", "2", null, timestamp, null)
        val holds = listOf(inactiveHold, activeHold)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)
        whenever(finder.find(any())).thenReturn(activeHold)

        val res = subject.release(deviceId, body)

        val expected = 204
        assertEquals(expected, res.status)
    }

}
