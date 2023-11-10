package api.holds

import database.Hold

class ActiveHoldFinder {

    fun find(
        holds: List<Hold>
    ): Hold? {
        val activeHold = holds.find { it.end == null }
        return activeHold
    }

}
