import api.accounts.PostAccount
import api.devices.PostDevice
import api.holds.PostHold
import database.AccountDao
import database.DeviceDao
import database.HoldDao
import io.dropwizard.Application
import io.dropwizard.jdbi3.JdbiFactory
import io.dropwizard.setup.Environment
import org.jdbi.v3.core.Jdbi

class HovedopgApp : Application<HovedopgConfiguration>() {

    override fun run(config: HovedopgConfiguration, env: Environment) {
        val factory = JdbiFactory()
        val jdbi: Jdbi = factory.build(env, config.database, "mysql")

        val accountDao = jdbi.onDemand(AccountDao::class.java)
        val deviceDao = jdbi.onDemand(DeviceDao::class.java)
        val holdDao = jdbi.onDemand(HoldDao::class.java)

        val postAccount = PostAccount(accountDao)
        val postDevice = PostDevice(deviceDao, accountDao)
        val postHold = PostHold(holdDao)

        //Register endpoints
        env.jersey().register(postAccount)
        env.jersey().register(postDevice)
        env.jersey().register(postHold)
    }

}

fun main(args: Array<String>) {
    HovedopgApp().run(*args)
}
