import api.account.PostAccount
import database.AccountDao
import io.dropwizard.Application
import io.dropwizard.jdbi3.JdbiFactory
import io.dropwizard.setup.Environment
import org.jdbi.v3.core.Jdbi

class HovedopgApp : Application<HovedopgConfiguration>() {

    override fun run(config: HovedopgConfiguration, env: Environment) {
        val factory = JdbiFactory()
        val jdbi: Jdbi = factory.build(env, config.database, "mysql")

        val accountDao = jdbi.onDemand(AccountDao::class.java)

        val postAccount = PostAccount(accountDao)

        //Register endpoints
        env.jersey().register(postAccount)
    }

}

fun main(args: Array<String>) {
    HovedopgApp().run(*args)
}
