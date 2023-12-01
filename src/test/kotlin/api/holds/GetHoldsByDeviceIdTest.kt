package api.holds

import database.Device
import database.DeviceDao
import database.Hold
import database.HoldDao
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.Timestamp
import java.time.Instant
import kotlin.test.assertEquals

class GetHoldsByDeviceIdTest {

    private lateinit var deviceDao: DeviceDao
    private lateinit var holdDao: HoldDao
    private lateinit var subject: GetHoldsByDeviceId

    @BeforeEach
    fun setup() {
        deviceDao = mock()
        holdDao = mock()

        subject = GetHoldsByDeviceId(deviceDao, holdDao)
    }

    @Test
    fun `return status 404 if the device was not found in the database`() {
        val input = "1"
        whenever(deviceDao.findById(any())).thenReturn(null)

        val res = subject.get(input)

        val expected = 404
        assertEquals(expected, res.status)
    }

    @Test
    fun `find all holds in the database with the input device id`() {
        val input = "1"
        val device = Device("1", "1")
        whenever(deviceDao.findById(any())).thenReturn(device)

        subject.get(input)

        verify(holdDao).findByDevice(input)
    }

    @Test
    fun `return status 200 if the request was successful`() {
        val input = "1"
        val device = Device("1", "1")
        val startInstant = Instant.parse("2022-12-15T15:00:00Z")
        val hold1 = Hold(1, "1", "1", "imei", Timestamp.from(startInstant), null)
        val hold2 = Hold(2, "1", "2", "imei", Timestamp.from(startInstant), null)
        val holds = listOf(hold1, hold2)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        val res = subject.get(input)

        val expected = 200
        assertEquals(expected, res.status)
    }

}
