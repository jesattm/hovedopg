package api.devices.measurements

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals

class IntervalCreatorTest {

    private lateinit var subject: IntervalCreator

    @BeforeEach
    fun setup() {
        subject = IntervalCreator()
    }

    @ParameterizedTest
    @CsvSource(
        "2022-12-14T15:00:00Z, 2022-12-15T15:00:00Z, 24",
        "2022-12-14T15:00:00Z, 2022-12-15T15:00:01Z, 25",
        "2022-12-14T15:00:00Z, 2022-12-15T14:59:59Z, 24",
        "2022-12-14T14:59:59Z, 2022-12-15T15:00:00Z, 25",
        )
    fun `return the correct amount of the intervals`(
        sinceString: String,
        untilString: String,
        expected: Int,
    ) {
        val resolution = Duration.ofMinutes(60)
        val since = Instant.parse(sinceString)
        val until = Instant.parse(untilString)

        val res = subject.createIntervals(resolution, since, until)

        assertEquals(expected, res.size)
    }

    @ParameterizedTest
    @CsvSource(
        "2022-12-14T15:00:00Z, 2022-12-15T15:00:00Z, 60",
        "2022-12-14T15:30:00Z, 2022-12-15T15:00:00Z, 60",
        "2022-12-14T15:00:00Z, 2022-12-15T15:30:00Z, 60",
        "2022-12-14T15:00:00Z, 2022-12-15T15:00:00Z, 17",
        )
    fun `return the correct duration of the first interval`(
        sinceString: String,
        untilString: String,
        resolutionLong: Long,
    ) {
        val resolution = Duration.ofMinutes(resolutionLong)
        val since = Instant.parse(sinceString)
        val until = Instant.parse(untilString)

        val res = subject.createIntervals(resolution, since, until)

        val intervalDuration = Duration.between(res.first().since, res.first().until)
        assertEquals(resolution, intervalDuration)
    }

    @ParameterizedTest
    @CsvSource(
        "2022-12-14T15:00:00Z, 2022-12-15T15:00:00Z, 60",
        "2022-12-14T15:30:00Z, 2022-12-15T15:00:00Z, 60",
        "2022-12-14T15:00:00Z, 2022-12-15T15:30:00Z, 60",
        "2022-12-14T15:00:00Z, 2022-12-15T15:00:00Z, 17",
    )
    fun `return the correct duration of the last interval`(
        sinceString: String,
        untilString: String,
        resolutionLong: Long,
    ) {
        val resolution = Duration.ofMinutes(resolutionLong)
        val since = Instant.parse(sinceString)
        val until = Instant.parse(untilString)

        val res = subject.createIntervals(resolution, since, until)

        val intervalDuration = Duration.between(res.last().since, res.last().until)
        assertEquals(resolution, intervalDuration)
    }

    @ParameterizedTest
    @CsvSource(
        "2022-12-14T15:00:00Z, 2022-12-15T15:00:00Z, 60",
        "2022-12-14T15:30:00Z, 2022-12-15T15:00:00Z, 60",
        "2022-12-14T15:00:00Z, 2022-12-15T15:30:00Z, 60",
        "2022-12-14T15:00:00Z, 2022-12-15T15:00:00Z, 17",
    )
    fun `return the correct timespan of the first interval`(
        sinceString: String,
        untilString: String,
        resolutionLong: Long,
    ) {
        val resolution = Duration.ofMinutes(resolutionLong)
        val since = Instant.parse(sinceString)
        val until = Instant.parse(untilString)

        val res = subject.createIntervals(resolution, since, until)

        val expectedSince = since
        val expectedUntil = since.plus(resolution)
        assertEquals(expectedSince, res.first().since)
        assertEquals(expectedUntil, res.first().until)
    }

}
