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

class ReleaseHoldTest {

    private lateinit var dao: HoldDao
    private lateinit var subject: ReleaseHold

    @BeforeEach
    fun setup() {
        dao = mock()

        subject = ReleaseHold(dao)
    }

    @Test
    fun `return status 404 if the hold was not found in the database`() {
        val id = 1
        val end = Instant.parse("2022-12-15T15:00:00Z")
        val body = ReleaseHoldBody(end)
        whenever(dao.findById(any())).thenReturn(null)

        val res = subject.release(id, body)

        val expected = 404
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
        val id = 1
        val end = Instant.parse(endString)
        val body = ReleaseHoldBody(end)
        val startInstant = Instant.parse("2022-12-15T15:00:00Z")
        val hold = Hold(id,"label", Timestamp.from(startInstant), null, 1)
        whenever(dao.findById(any())).thenReturn(hold)

        val res = subject.release(id, body)

        val expected = 422
        assertEquals(expected, res.status)
    }

    @Test
    fun `set new end with input values if the request is valid`() {
        val id = 1
        val end = Instant.parse("2022-12-16T15:00:00Z")
        val body = ReleaseHoldBody(end)
        val startInstant = Instant.parse("2022-12-15T15:00:00Z")
        val hold = Hold(id,"label", Timestamp.from(startInstant), null, 1)
        whenever(dao.findById(any())).thenReturn(hold)

        subject.release(id, body)

        verify(dao).setEnd(id, end)
    }

    @Test
    fun `return status 204 if the request is successful`() {
        val id = 1
        val end = Instant.parse("2022-12-16T15:00:00Z")
        val body = ReleaseHoldBody(end)
        val startInstant = Instant.parse("2022-12-15T15:00:00Z")
        val hold = Hold(id,"label", Timestamp.from(startInstant), null, 1)
        whenever(dao.findById(any())).thenReturn(hold)

        val res = subject.release(id, body)

        val expected = 204
        assertEquals(expected, res.status)
    }

}
