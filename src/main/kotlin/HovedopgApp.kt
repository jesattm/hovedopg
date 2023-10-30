import api.accounts.PostAccount
import api.claims.PostClaim
import database.AccountDao
import database.ClaimDao
import io.dropwizard.Application
import io.dropwizard.jdbi3.JdbiFactory
import io.dropwizard.setup.Environment
import org.jdbi.v3.core.Jdbi

class HovedopgApp : Application<HovedopgConfiguration>() {

    override fun run(config: HovedopgConfiguration, env: Environment) {
        val factory = JdbiFactory()
        val jdbi: Jdbi = factory.build(env, config.database, "mysql")

        val accountDao = jdbi.onDemand(AccountDao::class.java)
        val claimDao = jdbi.onDemand(ClaimDao::class.java)

        val postAccount = PostAccount(accountDao)
        val postClaim = PostClaim(claimDao, accountDao)

        //Register endpoints
        env.jersey().register(postAccount)
        env.jersey().register(postClaim)
    }

}

fun main(args: Array<String>) {
    HovedopgApp().run(*args)
}
