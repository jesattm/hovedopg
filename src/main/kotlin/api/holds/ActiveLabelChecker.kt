package api.holds

import database.Hold
import database.HoldDao

class ActiveLabelChecker(
    private val dao: HoldDao,
) {

    fun check(
        labelParam: String
    ): Boolean {
        val holds = dao.findByLabel(labelParam)

        return active(holds)
    }

    private fun active(
        holds: List<Hold>,
    ): Boolean {
        for (hold in holds) {
            if (hold.end == null) {
                return true
            }
        }

        return false
    }

}
