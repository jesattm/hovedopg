import io.dropwizard.Application
import io.dropwizard.jdbi3.JdbiFactory
import io.dropwizard.setup.Environment
import org.jdbi.v3.core.Jdbi

class HovedopgApp : Application<HovedopgConfiguration>() {

    override fun run(config: HovedopgConfiguration, env: Environment) {
        // Create a DataSource using a connection pool (BasicDataSource in this example)
        val factory = JdbiFactory()
        val jdbi: Jdbi = factory.build(env, config.database, "mysql")
//        val daoMeasurement = jdbi.onDemand(MeasurementsDao::class.java)
    }

}
