import api.accounts.CreateAccount
import api.devices.CreateDevice
import api.devices.GetDevicesByAccountId
import api.holds.ActiveHoldFinder
import api.holds.ActiveLabelChecker
import api.holds.CreateHold
import api.holds.GetHoldsByDeviceId
import api.holds.LatestEndFinder
import api.holds.ReleaseHold
import api.holds.ReplaceHold
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

        val checker = ActiveLabelChecker(holdDao)
        val holdFinder = ActiveHoldFinder()
        val endFinder = LatestEndFinder()

        val createAccount = CreateAccount(accountDao)
        val createDevice = CreateDevice(accountDao, deviceDao)
        val createHold = CreateHold(fakeLabelDatabase, checker, deviceDao, holdDao, holdFinder, endFinder)
        val releaseHold = ReleaseHold(deviceDao, holdDao, holdFinder)
        val getDevicesByAccountId = GetDevicesByAccountId(accountDao, deviceDao)
        val getHoldsByDeviceId = GetHoldsByDeviceId(deviceDao, holdDao)
        val replaceHold = ReplaceHold(fakeLabelDatabase, checker, deviceDao, holdDao, holdFinder)

        //Register endpoints
        env.jersey().register(createAccount)
        env.jersey().register(createDevice)
        env.jersey().register(createHold)
        env.jersey().register(releaseHold)
        env.jersey().register(getDevicesByAccountId)
        env.jersey().register(getHoldsByDeviceId)
        env.jersey().register(replaceHold)
    }

}

fun main(args: Array<String>) {
    HovedopgApp().run(*args)
}
