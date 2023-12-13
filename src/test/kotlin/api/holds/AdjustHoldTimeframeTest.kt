package api.holds

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import database.Hold
import database.HoldDao
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.sql.Timestamp
import java.time.Instant
import kotlin.test.assertEquals

class AdjustHoldTimeframeTest {

    private lateinit var holdDao: HoldDao
    private lateinit var subject: AdjustHoldTimeframe

    @BeforeEach
    fun setup() {
        holdDao = mock()

        subject = AdjustHoldTimeframe(holdDao)
    }

    @ParameterizedTest
    @CsvSource(
        "2022-12-14T15:00:00Z",
        "2022-12-15T15:00:00Z",
    )
    fun `return status 422 if the start input is not before the end input`(
        startString: String,
    ) {
        val id = 1
        val startInput = Instant.parse(startString)
        val endInput = Instant.parse("2022-12-14T15:00:00Z")
        val body = AdjustHoldTimeframeBody(startInput, endInput)
        val startTimestamp = Timestamp.from(Instant.parse("2022-12-13T15:00:00Z"))
        val endTimestamp = Timestamp.from(Instant.parse("2022-12-16T15:00:00Z"))
        val hold = Hold(1, "device id", "label", "imei", startTimestamp, endTimestamp)
        whenever(holdDao.findById(any())).thenReturn(hold)

        val res = subject.adjust(id, body)

        val expected = 422
        assertEquals(expected, res.status)
    }

    @ParameterizedTest
    @CsvSource(
        "2022-12-14T15:00:00Z",
        "2022-12-15T15:00:00Z",
    )
    fun `return status 409 if the start input is not before the hold's end`(
        startInputString: String,
        ) {
        val id = 1
        val startInput = Instant.parse(startInputString)
        val body = AdjustHoldTimeframeBody(startInput, null)
        val startTimestamp = Timestamp.from(Instant.parse("2022-12-13T15:00:00Z"))
        val endTimestamp = Timestamp.from(Instant.parse("2022-12-14T15:00:00Z"))
        val hold = Hold(1, "device id", "label", "imei", startTimestamp, endTimestamp)
        whenever(holdDao.findById(any())).thenReturn(hold)

        val res = subject.adjust(id, body)

        val expected = 409
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 400 if the previous hold's end is null`() {
        val id = 1
        val startInput = Instant.parse("2022-12-14T10:00:00Z")
        val body = AdjustHoldTimeframeBody(startInput, null)
        val currentHold = Hold(2, "device id", "label", "imei", Timestamp.from(Instant.parse("2022-12-13T15:00:00Z")), Timestamp.from(Instant.parse("2022-12-14T15:00:00Z")))
        val endTimestamp = null
        val previousHold = Hold(1, "device id", "label", "imei", Timestamp.from(Instant.parse("2022-12-10T15:00:00Z")), endTimestamp)
        val holds = listOf(previousHold, currentHold)
        whenever(holdDao.findById(any())).thenReturn(currentHold)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        val res = subject.adjust(id, body)

        val expected = 400
        assertEquals(expected, res.status)
    }

    @ParameterizedTest
    @CsvSource(
        "2022-12-12T15:00:00Z",
        "2022-12-11T15:00:00Z",
        "2022-12-01T15:00:00Z",
    )
    fun `return status 409 if the start input is before the previous hold's end`(
        startInputString: String,
    ) {
        val id = 1
        val startInput = Instant.parse(startInputString)
        val body = AdjustHoldTimeframeBody(startInput, null)
        val currentHold = Hold(2, "device id", "label", "imei", Timestamp.from(Instant.parse("2022-12-13T15:00:00Z")), Timestamp.from(Instant.parse("2022-12-14T15:00:00Z")))
        val previousHold = Hold(1, "device id", "label", "imei", Timestamp.from(Instant.parse("2022-12-10T15:00:00Z")), Timestamp.from(Instant.parse("2022-12-12T15:00:00Z")))
        val holds = listOf(previousHold, currentHold)
        whenever(holdDao.findById(any())).thenReturn(currentHold)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        val res = subject.adjust(id, body)

        val expected = 409
        assertEquals(expected, res.status)
    }

    @Test
    fun `return status 422 if end input is not null and the hold's end is null`() {
        val id = 1
        val endInput = Instant.parse("2022-12-14T10:00:00Z")
        val body = AdjustHoldTimeframeBody(null, endInput)
        val hold = Hold(1, "device id", "label", "imei", Timestamp.from(Instant.parse("2022-12-13T15:00:00Z")), null)
        val holds = listOf(hold)
        whenever(holdDao.findById(any())).thenReturn(hold)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        val res = subject.adjust(id, body)

        val expected = 422
        assertEquals(expected, res.status)
    }

    @ParameterizedTest
    @CsvSource(
        "2022-12-13T15:00:00Z",
        "2022-12-12T15:00:00Z",
    )
    fun `return status 409 if the end input is not after the hold's start`(
        endInputString: String,
    ) {
        val id = 1
        val endInput = Instant.parse(endInputString)
        val body = AdjustHoldTimeframeBody(null, endInput)
        val hold = Hold(1, "device id", "label", "imei", Timestamp.from(Instant.parse("2022-12-13T15:00:00Z")), Timestamp.from(Instant.parse("2022-12-15T15:00:00Z")))
        val holds = listOf(hold)
        whenever(holdDao.findById(any())).thenReturn(hold)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        val res = subject.adjust(id, body)

        val expected = 409
        assertEquals(expected, res.status)
    }

    @ParameterizedTest
    @CsvSource(
        "2022-12-16T15:00:00Z",
        "2022-12-17T15:00:00Z",
    )
    fun `return status 409 if the end input is not before the next hold's start`(
        endInputString: String,
    ) {
        val id = 1
        val endInput = Instant.parse(endInputString)
        val body = AdjustHoldTimeframeBody(null, endInput)
        val currentHold = Hold(1, "device id", "label", "imei", Timestamp.from(Instant.parse("2022-12-13T15:00:00Z")), Timestamp.from(Instant.parse("2022-12-15T15:00:00Z")))
        val nextHold = Hold(2, "device id", "label", "imei", Timestamp.from(Instant.parse("2022-12-16T15:00:00Z")), null)
        val holds = listOf(currentHold, nextHold)
        whenever(holdDao.findById(any())).thenReturn(currentHold)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        val res = subject.adjust(id, body)

        val expected = 409
        assertEquals(expected, res.status)
    }

    @Test
    fun `update the hold's start if the request is valid and there are no other holds in the device`() {
        val id = 1
        val startInput = Instant.parse("2022-12-14T15:00:00Z")
        val endInput = null
        val body = AdjustHoldTimeframeBody(startInput, endInput)
        val hold = Hold(1, "device id", "label", "imei", Timestamp.from(Instant.parse("2022-12-13T15:00:00Z")), null)
        val holds = listOf(hold)
        whenever(holdDao.findById(any())).thenReturn(hold)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        subject.adjust(id, body)

        verify(holdDao).update(id, startInput, endInput)
    }

    @ParameterizedTest
    @CsvSource(
        "2022-12-14T15:00:00Z",
        "2022-12-15T15:00:00Z",
        "2022-12-16T15:00:00Z",
    )
    fun `update the hold's end if the request is valid and there are no other holds in the device`(
        endInputString: String,
        ) {
        val id = 1
        val startInput = null
        val endInput = Instant.parse(endInputString)
        val body = AdjustHoldTimeframeBody(startInput, endInput)
        val hold = Hold(1, "device id", "label", "imei", Timestamp.from(Instant.parse("2022-12-13T15:00:00Z")), Timestamp.from(Instant.parse("2022-12-15T15:00:00Z")))
        val holds = listOf(hold)
        whenever(holdDao.findById(any())).thenReturn(hold)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        subject.adjust(id, body)

        verify(holdDao).update(id, startInput, endInput)
    }

    @Test
    fun `update the hold's start and end if the request is valid and there are other holds in the device`() {
        val id = 1
        val startInput = Instant.parse("2022-12-11T20:00:00Z")
        val endInput = Instant.parse("2022-12-14T10:00:00Z")
        val body = AdjustHoldTimeframeBody(startInput, endInput)
        val previousHold = Hold(1, "device id", "label", "imei", Timestamp.from(Instant.parse("2022-12-10T15:00:00Z")), Timestamp.from(Instant.parse("2022-12-11T15:00:00Z")))
        val currentHold = Hold(2, "device id", "label", "imei", Timestamp.from(Instant.parse("2022-12-12T15:00:00Z")), Timestamp.from(Instant.parse("2022-12-13T15:00:00Z")))
        val nextHold = Hold(3, "device id", "label", "imei", Timestamp.from(Instant.parse("2022-12-14T15:00:00Z")), null)
        val holds = listOf(previousHold, currentHold, nextHold)
        whenever(holdDao.findById(any())).thenReturn(currentHold)
        whenever(holdDao.findByDevice(any())).thenReturn(holds)

        subject.adjust(id, body)

        verify(holdDao).update(id, startInput, endInput)
    }

}
