package api.holds

import database.Hold
import java.time.Instant

class LastestEndFinder {

    fun find(
        holds: List<Hold>
    ): Instant? {
        if (holds.isEmpty()) {
            return null
        }

        val latestEnd = holds
            .mapNotNull { it.end }
            .map { it.toInstant() }
            .maxBy { it }

        return latestEnd
    }

}
