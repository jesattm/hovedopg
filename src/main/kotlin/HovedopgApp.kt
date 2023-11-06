import api.accounts.CreateAccount
import api.devices.CreateDevice
import api.holds.CreateHold
import api.holds.ActiveLabelChecker
import database.AccountDao
import database.DeviceDao
import database.HoldDao
import database.labels.FakeLabelsDatabase
import io.dropwizard.Application
import io.dropwizard.jdbi3.JdbiFactory
import io.dropwizard.setup.Environment
import org.jdbi.v3.core.Jdbi

class HovedopgApp : Application<HovedopgConfiguration>() {

    override fun run(config: HovedopgConfiguration, env: Environment) {
        val factory = JdbiFactory()
        val jdbi: Jdbi = factory.build(env, config.database, "mysql")

        // Use fake label database
        val fakeLabelDatabase = FakeLabelsDatabase()
        fakeLabelDatabase.generateLabels()

        val accountDao = jdbi.onDemand(AccountDao::class.java)
        val deviceDao = jdbi.onDemand(DeviceDao::class.java)
        val holdDao = jdbi.onDemand(HoldDao::class.java)

        val createAccount = CreateAccount(accountDao)
        val createDevice = CreateDevice(deviceDao, accountDao)
        val createHold = CreateHold(
            holdDao,
            fakeLabelDatabase,
            deviceDao,
            ActiveLabelChecker(holdDao),
            )

        //Register endpoints
        env.jersey().register(createAccount)
        env.jersey().register(createDevice)
        env.jersey().register(createHold)
    }

}

fun main(args: Array<String>) {
    HovedopgApp().run(*args)
}
