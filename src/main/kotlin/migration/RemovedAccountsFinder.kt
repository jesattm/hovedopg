package migration

class RemovedAccountsFinder {

    fun find(
        events: List<AccountEvent>
    ): List<String> {
        val grouped = events.groupBy { it.accountId }
        val list = grouped.values.toList()

        val removed = list.filter { it.size == 2 }
        val removedList = removed.map { it.first().accountId }
        return removedList
    }

}
