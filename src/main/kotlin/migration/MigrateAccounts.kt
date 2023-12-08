package migration

import database.AccountDao

class MigrateAccounts(
    private val dao: AccountDao
) {

    fun migrate(events: List<AccountEvent>) {
        val grouped = groupById(events)
        val filtered = filterRemovedAccounts(grouped)
        val list = filtered.flatten()

        println("-------------- Migrating accounts.. --------------")
        var count = 0
        for (event in list) {
            dao.create(event.accountId, event.apiKey!!)
            if (count % 100 == 0) {
                println("$count accounts migrated..")
            }
            count++
        }
        println("-------------- Migration of accounts finished. $count accounts migrated. --------------")
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
