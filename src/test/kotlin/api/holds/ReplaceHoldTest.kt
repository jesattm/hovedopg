package api.holds

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import database.Device
import database.DeviceDao
import database.Hold
import database.HoldDao
import database.labels.FakeLabelsDatabase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Timestamp
import java.time.Instant
import kotlin.test.assertEquals

class ReplaceHoldTest {

    private lateinit var labels: FakeLabelsDatabase
    private lateinit var checker: ActiveLabelChecker
    private lateinit var deviceDao: DeviceDao
    private lateinit var holdDao: HoldDao
    private lateinit var finder: ActiveHoldFinder
    private lateinit var subject: ReplaceHold

    @BeforeEach
    fun setup() {
        labels = mock()
        checker = mock()
        deviceDao = mock()
        holdDao = mock()
        finder = mock()

        subject = ReplaceHold(labels, checker, deviceDao, holdDao, finder)
    }

    @Test
    fun `return status 404 if the device was not found in the database`() {
        val deviceId = 1
        val retiredSince = Instant.parse("2022-12-15T15:00:00Z")
        val replacementLabel = "QWER1234"
        val claimedSince = Instant.parse("2022-12-16T15:00:00Z")
        val body = ReplaceHoldBody(retiredSince, replacementLabel, claimedSince)
        whenever(deviceDao.findById(any())).thenReturn(null)

        val res = subject.replace(deviceId, body)

        val expected = 404
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 404 if the device has no holds`() {
        val deviceId = 1
        val retiredSince = Instant.parse("2022-12-15T15:00:00Z")
        val replacementLabel = "QWER1234"
        val claimedSince = Instant.parse("2022-12-16T15:00:00Z")
        val body = ReplaceHoldBody(retiredSince, replacementLabel, claimedSince)
        val device = Device(1, 1)
        val holds: List<Hold> = listOf()
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        val res = subject.replace(deviceId, body)

        val expected = 404
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 409 if there is no active hold on the device`() {
        val deviceId = 1
        val retiredSince = Instant.parse("2022-12-15T15:00:00Z")
        val replacementLabel = "QWER1234"
        val claimedSince = Instant.parse("2022-12-16T15:00:00Z")
        val body = ReplaceHoldBody(retiredSince, replacementLabel, claimedSince)
        val device = Device(1, 1)
        val timestamp = Timestamp.from(Instant.parse("2022-12-15T15:00:00Z"))
        val hold = Hold(deviceId,"label", timestamp, timestamp, 1)
        val holds: List<Hold> = listOf(hold)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)
        whenever(finder.find(any())).thenReturn(null)

        val res = subject.replace(deviceId, body)

        val expected = 409
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 422 if retire time is not after the previous holds start time`() {
        val deviceId = 1
        val retiredSince = Instant.parse("2022-12-14T15:00:00Z")
        val replacementLabel = "QWER1234"
        val claimedSince = Instant.parse("2022-12-16T15:00:00Z")
        val body = ReplaceHoldBody(retiredSince, replacementLabel, claimedSince)
        val device = Device(1, 1)
        val timestamp = Timestamp.from(Instant.parse("2022-12-15T15:00:00Z"))
        val inactiveHold = Hold(1,"label", timestamp, timestamp, 1)
        val activeHold = Hold(2,"label", timestamp, null, 1)
        val holds: List<Hold> = listOf(inactiveHold, activeHold)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)
        whenever(finder.find(any())).thenReturn(activeHold)

        val res = subject.replace(deviceId, body)

        val expected = 422
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 422 if the replacement label is not 8 characters`() {
        val deviceId = 1
        val retiredSince = Instant.parse("2022-12-16T15:00:00Z")
        val replacementLabel = "QWER123"
        val claimedSince = Instant.parse("2022-12-17T15:00:00Z")
        val body = ReplaceHoldBody(retiredSince, replacementLabel, claimedSince)
        val device = Device(1, 1)
        val timestamp = Timestamp.from(Instant.parse("2022-12-15T15:00:00Z"))
        val inactiveHold = Hold(1,"label", timestamp, timestamp, 1)
        val activeHold = Hold(2,"label", timestamp, null, 1)
        val holds: List<Hold> = listOf(inactiveHold, activeHold)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)
        whenever(finder.find(any())).thenReturn(activeHold)

        val res = subject.replace(deviceId, body)

        val expected = 422
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 404 if the replacement label was not found in the label database`() {
        val deviceId = 1
        val retiredSince = Instant.parse("2022-12-16T15:00:00Z")
        val replacementLabel = "QWER1234"
        val claimedSince = Instant.parse("2022-12-17T15:00:00Z")
        val body = ReplaceHoldBody(retiredSince, replacementLabel, claimedSince)
        val device = Device(1, 1)
        val timestamp = Timestamp.from(Instant.parse("2022-12-15T15:00:00Z"))
        val inactiveHold = Hold(1,"label", timestamp, timestamp, 1)
        val activeHold = Hold(2,"label", timestamp, null, 1)
        val holds: List<Hold> = listOf(inactiveHold, activeHold)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)
        whenever(finder.find(any())).thenReturn(activeHold)
        whenever(labels.find(any())).thenReturn(null)

        val res = subject.replace(deviceId, body)

        val expected = 404
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 409 if the replacement label is in use`() {
        val deviceId = 1
        val retiredSince = Instant.parse("2022-12-16T15:00:00Z")
        val replacementLabel = "QWER1234"
        val claimedSince = Instant.parse("2022-12-17T15:00:00Z")
        val body = ReplaceHoldBody(retiredSince, replacementLabel, claimedSince)
        val device = Device(1, 1)
        val timestamp = Timestamp.from(Instant.parse("2022-12-15T15:00:00Z"))
        val inactiveHold = Hold(1,"label", timestamp, timestamp, 1)
        val activeHold = Hold(2,"label", timestamp, null, 1)
        val holds: List<Hold> = listOf(inactiveHold, activeHold)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)
        whenever(finder.find(any())).thenReturn(activeHold)
        whenever(labels.find(any())).thenReturn("QWER1234")
        whenever(checker.check(any())).thenReturn(true)

        val res = subject.replace(deviceId, body)

        val expected = 409
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 422 if claim time is not after retire time`() {
        val deviceId = 1
        val retiredSince = Instant.parse("2022-12-16T15:00:00Z")
        val replacementLabel = "QWER1234"
        val claimedSince = Instant.parse("2022-12-15T15:00:00Z")
        val body = ReplaceHoldBody(retiredSince, replacementLabel, claimedSince)
        val device = Device(1, 1)
        val timestamp = Timestamp.from(Instant.parse("2022-12-15T15:00:00Z"))
        val inactiveHold = Hold(1,"label", timestamp, timestamp, 1)
        val activeHold = Hold(2,"label", timestamp, null, 1)
        val holds: List<Hold> = listOf(inactiveHold, activeHold)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)
        whenever(finder.find(any())).thenReturn(activeHold)
        whenever(labels.find(any())).thenReturn("QWER1234")
        whenever(checker.check(any())).thenReturn(false)

        val res = subject.replace(deviceId, body)

        val expected = 422
        assertEquals(expected, res.status)
    }

    @Test
    fun `set end attribute on the active hold with the input retire value if the request is valid`() {
        val deviceId = 1
        val retiredSince = Instant.parse("2022-12-16T15:00:00Z")
        val replacementLabel = "QWER1234"
        val claimedSince = Instant.parse("2022-12-17T15:00:00Z")
        val body = ReplaceHoldBody(retiredSince, replacementLabel, claimedSince)
        val device = Device(1, 1)
        val timestamp = Timestamp.from(Instant.parse("2022-12-15T15:00:00Z"))
        val inactiveHold = Hold(1,"label", timestamp, timestamp, 1)
        val activeHold = Hold(2,"label", timestamp, null, 1)
        val holds: List<Hold> = listOf(inactiveHold, activeHold)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)
        whenever(finder.find(any())).thenReturn(activeHold)
        whenever(labels.find(any())).thenReturn("QWER1234")
        whenever(checker.check(any())).thenReturn(false)

        subject.replace(deviceId, body)

        verify(holdDao).setEnd(activeHold.id, retiredSince)
    }

    @Test
    fun `create hold with the input values if the request is valid`() {
        val deviceId = 1
        val retiredSince = Instant.parse("2022-12-16T15:00:00Z")
        val replacementLabel = "QWER1234"
        val claimedSince = Instant.parse("2022-12-17T15:00:00Z")
        val body = ReplaceHoldBody(retiredSince, replacementLabel, claimedSince)
        val device = Device(1, 1)
        val timestamp = Timestamp.from(Instant.parse("2022-12-15T15:00:00Z"))
        val inactiveHold = Hold(1,"label", timestamp, timestamp, 1)
        val activeHold = Hold(2,"label", timestamp, null, 1)
        val holds: List<Hold> = listOf(inactiveHold, activeHold)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)
        whenever(finder.find(any())).thenReturn(activeHold)
        whenever(labels.find(any())).thenReturn("QWER1234")
        whenever(checker.check(any())).thenReturn(false)

        subject.replace(deviceId, body)

        verify(holdDao).create(replacementLabel, claimedSince, null, deviceId)
    }

    @Test
    fun `return status 201 if the request is successful`() {
        val deviceId = 1
        val retiredSince = Instant.parse("2022-12-16T15:00:00Z")
        val replacementLabel = "QWER1234"
        val claimedSince = Instant.parse("2022-12-17T15:00:00Z")
        val body = ReplaceHoldBody(retiredSince, replacementLabel, claimedSince)
        val device = Device(1, 1)
        val timestamp = Timestamp.from(Instant.parse("2022-12-15T15:00:00Z"))
        val inactiveHold = Hold(1,"label", timestamp, timestamp, 1)
        val activeHold = Hold(2,"label", timestamp, null, 1)
        val holds: List<Hold> = listOf(inactiveHold, activeHold)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)
        whenever(finder.find(any())).thenReturn(activeHold)
        whenever(labels.find(any())).thenReturn("QWER1234")
        whenever(checker.check(any())).thenReturn(false)

        val res = subject.replace(deviceId, body)

        val expected = 201
        assertEquals(expected, res.status)
    }

}
