import api.accounts.CreateAccount
import api.accounts.DeleteAccount
import api.devices.CreateDevice
import api.devices.DeleteDevice
import api.devices.GetDevicesByAccountId
import api.devices.measurements.GetMeasurements
import api.devices.measurements.IntervalCreator
import api.devices.measurements.MeasurementsAggregator
import api.devices.measurements.MeasurementsHoldsCombiner
import api.holds.ActiveHoldFinder
import api.holds.ActiveLabelChecker
import api.holds.AdjustHoldTimeframe
import api.holds.CreateHold
import api.holds.DeleteHold
import api.holds.GetHoldsByDeviceId
import api.holds.LatestEndFinder
import api.holds.ReleaseHold
import api.holds.ReplaceHold
import com.fasterxml.jackson.databind.SerializationFeature
import database.AccountDao
import database.DeviceDao
import database.HoldDao
import database.measurements.FakeMeasurementsDatabase
import database.stations.FakeStationsDatabase
import io.dropwizard.Application
import io.dropwizard.jdbi3.JdbiFactory
import io.dropwizard.setup.Environment
import org.jdbi.v3.core.Jdbi

class HovedopgApp : Application<HovedopgConfiguration>() {

    override fun run(config: HovedopgConfiguration, env: Environment) {
        env.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

        val factory = JdbiFactory()
        val jdbi: Jdbi = factory.build(env, config.database, "mysql")

        // Use fake label database
        val fakeStationsDatabase = FakeStationsDatabase()
        fakeStationsDatabase.generateStations()

        // Use fake measurements database
        val fakeMeasurementsDatabase = FakeMeasurementsDatabase()

        val accountDao = jdbi.onDemand(AccountDao::class.java)
        val deviceDao = jdbi.onDemand(DeviceDao::class.java)
        val holdDao = jdbi.onDemand(HoldDao::class.java)

        val checker = ActiveLabelChecker(holdDao)
        val holdFinder = ActiveHoldFinder()
        val endFinder = LatestEndFinder()
        val combiner = MeasurementsHoldsCombiner(fakeMeasurementsDatabase)
        val creator = IntervalCreator()
        val aggregator = MeasurementsAggregator()

        val createAccount = CreateAccount(accountDao)
        val createDevice = CreateDevice(accountDao, deviceDao)
        val createHold = CreateHold(fakeStationsDatabase, checker, deviceDao, holdDao, holdFinder, endFinder)
        val releaseHold = ReleaseHold(deviceDao, holdDao, holdFinder)
        val getDevicesByAccountId = GetDevicesByAccountId(accountDao, deviceDao)
        val getHoldsByDeviceId = GetHoldsByDeviceId(deviceDao, holdDao)
        val replaceHold = ReplaceHold(fakeStationsDatabase, checker, deviceDao, holdDao, holdFinder)
        val getMeasurements = GetMeasurements(deviceDao, holdDao, combiner, creator, aggregator)
        val deleteHold = DeleteHold(holdDao)
        val deleteDevice = DeleteDevice(deviceDao)
        val deleteAccount = DeleteAccount(accountDao)
        val adjustHoldTimeframe = AdjustHoldTimeframe(holdDao)

        //Register endpoints
        env.jersey().register(createAccount)
        env.jersey().register(createDevice)
        env.jersey().register(createHold)
        env.jersey().register(releaseHold)
        env.jersey().register(getDevicesByAccountId)
        env.jersey().register(getHoldsByDeviceId)
        env.jersey().register(replaceHold)
        env.jersey().register(getMeasurements)
        env.jersey().register(deleteHold)
        env.jersey().register(deleteDevice)
        env.jersey().register(deleteAccount)
        env.jersey().register(adjustHoldTimeframe)
    }

}

fun main(args: Array<String>) {
    HovedopgApp().run(*args)
}
