import io.dropwizard.Configuration
import io.dropwizard.db.DataSourceFactory

class HovedopgConfiguration : Configuration() {

    var database: DataSourceFactory = DataSourceFactory()

}
