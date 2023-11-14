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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import kotlin.test.assertEquals

class CreateHoldTest {

    private lateinit var labels: FakeLabelsDatabase
    private lateinit var checker: ActiveLabelChecker
    private lateinit var deviceDao: DeviceDao
    private lateinit var holdDao: HoldDao
    private lateinit var holdFinder: ActiveHoldFinder
    private lateinit var endFinder: LatestEndFinder
    private lateinit var subject: CreateHold

    @BeforeEach
    fun setup() {
        labels = mock()
        checker = mock()
        deviceDao = mock()
        holdDao = mock()
        holdFinder = mock()
        endFinder = mock()

        subject = CreateHold(labels, checker, deviceDao, holdDao, holdFinder, endFinder)
    }

    @ParameterizedTest
    @CsvSource(
        "QWER123",
        "QWER12345",
    )
    fun `return status 422 if the label is not 8 characters`(
        label: String
    ) {
        val deviceId = 1
        val start = Instant.parse("2022-12-01T15:00:00Z")
        val body = CreateHoldBody(label, start)

        val res = subject.create(deviceId, body)

        val expected = 422
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 404 if the label was not found in the database`() {
        val deviceId = 1
        val start = Instant.parse("2022-12-01T15:00:00Z")
        val body = CreateHoldBody("QWER1234", start)
        whenever(labels.find(any())).thenReturn(null)

        val res = subject.create(deviceId, body)

        val expected = 404
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 409 if the label is in use`() {
        val deviceId = 1
        val start = Instant.parse("2022-12-01T15:00:00Z")
        val label = "QWER1234"
        val body = CreateHoldBody(label, start)
        whenever(labels.find(any())).thenReturn(label)
        whenever(checker.check(any())).thenReturn(true)

        val res = subject.create(deviceId, body)

        val expected = 409
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 404 if the device was not found in the database`() {
        val deviceId = 1
        val start = Instant.parse("2022-12-01T15:00:00Z")
        val label = "QWER1234"
        val body = CreateHoldBody(label, start)
        whenever(labels.find(any())).thenReturn(label)
        whenever(checker.check(any())).thenReturn(false)
        whenever(deviceDao.findById(any())).thenReturn(null)

        val res = subject.create(deviceId, body)

        val expected = 404
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 409 if the device has an active hold`() {
        val deviceId = 1
        val start = Instant.parse("2022-12-01T15:00:00Z")
        val label = "QWER1234"
        val body = CreateHoldBody(label, start)
        val device = Device(1, 1)
        val hold = Hold(1,"label", Timestamp.valueOf(LocalDateTime.now()), null, 1)
        whenever(labels.find(any())).thenReturn(label)
        whenever(checker.check(any())).thenReturn(false)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdFinder.find(any())).thenReturn(hold)

        val res = subject.create(deviceId, body)

        val expected = 409
        assertEquals(expected, res.status)
    }

    @ParameterizedTest
    @CsvSource(
        "2022-12-14T15:00:00Z",
        "2022-12-15T15:00:00Z",
    )
    fun `return status 422 if the device has a previous hold with an end after the start input`(
        startString: String
    ) {
        val deviceId = 1
        val startInstant = Instant.parse(startString)
        val label = "QWER1234"
        val body = CreateHoldBody(label, startInstant)
        val device = Device(1, 1)
        val end = Instant.parse("2022-12-15T15:00:00Z")
        whenever(labels.find(any())).thenReturn(label)
        whenever(checker.check(any())).thenReturn(false)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdFinder.find(any())).thenReturn(null)
        whenever(endFinder.find(any())).thenReturn(end)

        val res = subject.create(deviceId, body)

        val expected = 422
        assertEquals(expected, res.status)
    }



    @Test
    fun `create a hold with the input data if the request is valid`() {
        val deviceId = 1
        val start = Instant.parse("2022-12-16T15:00:00Z")
        val label = "QWER1234"
        val body = CreateHoldBody(label, start)
        val device = Device(1, 1)
        val end = Instant.parse("2022-12-15T15:00:00Z")
        whenever(labels.find(any())).thenReturn(label)
        whenever(checker.check(any())).thenReturn(false)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdFinder.find(any())).thenReturn(null)
        whenever(endFinder.find(any())).thenReturn(end)

        subject.create(deviceId, body)

        verify(holdDao).create(label, start, null, deviceId)
    }

    @Test
    fun `return status 201 if the request is successful`() {
        val deviceId = 1
        val start = Instant.parse("2022-12-16T15:00:00Z")
        val label = "QWER1234"
        val body = CreateHoldBody(label, start)
        val device = Device(1, 1)
        val end = Instant.parse("2022-12-15T15:00:00Z")
        whenever(labels.find(any())).thenReturn(label)
        whenever(checker.check(any())).thenReturn(false)
        whenever(deviceDao.findById(any())).thenReturn(device)
        whenever(holdFinder.find(any())).thenReturn(null)
        whenever(endFinder.find(any())).thenReturn(end)

        val res = subject.create(deviceId, body)

        val expected = 201
        assertEquals(expected, res.status)
    }

}
