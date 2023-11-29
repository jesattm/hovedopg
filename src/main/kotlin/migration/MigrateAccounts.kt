package migration

import database.AccountDao

class MigrateAccounts(
    private val dao: AccountDao
) {

    fun migrate(events: List<AccountEvent>) {
        val grouped = groupById(events)
        val filtered = filterRemovedAccounts(grouped)
        val list = filtered.flatten()

        for (event in list) {
            dao.create(event.accountId, event.apiKey!!)
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
