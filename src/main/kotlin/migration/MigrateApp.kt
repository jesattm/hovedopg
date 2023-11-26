package migration

import database.AccountDao
import database.DeviceDao
import database.HoldDao
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.SqlObjectPlugin

class MigrateApp {

    fun run() {
        val jdbi = Jdbi.create(
            "jdbc:mysql://localhost:3306/hovedopg_DB?serverTimezone=UTC",
            "root",
            "root",
        )
        jdbi.installPlugin(SqlObjectPlugin())

        val accountDao = jdbi.onDemand(AccountDao::class.java)
        val deviceDao = jdbi.onDemand(DeviceDao::class.java)
        val holdDao = jdbi.onDemand(HoldDao::class.java)

        val eventMapper = EventMapper()
        val migrateAccounts = MigrateAccounts(accountDao)
        val migrateDevices = MigrateDevices(deviceDao, holdDao)
        val removedAccountsFinder = RemovedAccountsFinder()

        // Run
        val events = eventMapper.map()

        migrateAccounts.migrate(events.accountEvents)

        val removedAccounts = removedAccountsFinder.find(events.accountEvents)
        migrateDevices.migrate(events.deviceEvents, removedAccounts)
    }

}

fun main() {
    MigrateApp().run()
}
