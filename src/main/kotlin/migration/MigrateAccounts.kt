package migration

import database.AccountDao
import java.time.Instant

class MigrateAccounts(
    private val dao: AccountDao
) {

    fun migrate(events: List<AccountEvent>) {
        val grouped = groupById(events)
        val filtered = filterRemovedAccounts(grouped)
        val list = filtered.flatten()

        for (event in list) {
            val instant = Instant.parse(event.timestamp)
            dao.create(event.accountId, event.apiKey!!, instant)
        }
    }

    private fun groupById(
        events: List<AccountEvent>
    ): List<List<AccountEvent>> {
        val grouped = events.groupBy { it.accountId }
        val list = grouped.values.toList()
        return list
    }

    private fun filterRemovedAccounts(
        events: List<List<AccountEvent>>,
    ): List<List<AccountEvent>> {
        val filtered = events.filterNot { it.size == 2 }
        return filtered
    }

}
