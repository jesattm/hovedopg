package api.devices.measurements

import database.DeviceDao
import database.HoldDao
import database.measurements.TimeAndValue
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.validation.constraints.NotNull
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/devices/{deviceId}/measurements")
class GetMeasurements(
    private val deviceDao: DeviceDao,
    private val holdDao: HoldDao,
    private val combiner: MeasurementHoldCombiner,
    private val creator: IntervalCreator,
    private val aggregator: MeasurementAggregator,
) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun get(
        @PathParam("deviceId")
        deviceId: String,
        @QueryParam("sensor")
        @NotNull
        sensorParam: String,
        @QueryParam("since")
        @NotNull
        sinceParam: String,
        @QueryParam("until")
        @NotNull
        untilParam: String,
        @QueryParam("resolution")
        @NotNull
        resolutionParam: Long,
    ): Response {
        val sensor = toSensor(sensorParam)
        if (sensor == null) {
            return Response.status(400, "Invalid sensor.").build()
        }

        val since = parsedAsInstant(sinceParam)
        if (since == null) {
            return Response
                .status(400, "Since cannot be parsed as an Instant.")
                .build()
        }

        val until = parsedAsInstant(untilParam)
        if (until == null) {
            return Response
                .status(400, "Until cannot be parsed as an Instant.")
                .build()
        }

        if (!until.isAfter(since.plus(Duration.ofMinutes(10)))) {
            return Response
                .status(422, "Until must be over 10 minutes after since.")
                .build()
        }

        if (resolutionParam < 10) {
            return Response
                .status(422, "Resolution must be at least 10 minutes.")
                .build()
        }

        val timeBetween = Duration.between(since, until)
        if (resolutionParam > timeBetween.toMinutes()) {
            return Response.status(422,
                    "Resolution must not be more than time between since and until.")
                .build()
        }

        val querySize = countQuerySize(since, until, resolutionParam)
        if (querySize >= 1825) {
            return Response.status(400, "Request too big.").build()
        }

        val device = deviceDao.findById(deviceId)
        if (device == null) {
            return Response.status(404, "Device not found.").build()
        }

        val holds = holdDao.findByDevice(deviceId)
        if (holds.isEmpty()) {
            return Response.status(404, "Device has no holds.").build()
        }

        val measurements = combiner.combine(holds, sensor, since, until)

        val intervals = creator.createIntervals(Duration.ofMinutes(resolutionParam), since, until)

        val aggregated = aggregator.aggregate(intervals, measurements, sensor)

        val response = GetMeasurementsResponse(
            deviceId,
            sensor.measurementName,
            resolutionParam,
            aggregated,
        )
        return Response.status(200).entity(response).build()
    }

    private fun parsedAsInstant(timeString: String): Instant? {
        try {
            return Instant.parse(timeString)
        } catch (e: DateTimeParseException) {
            return null
        }
    }

    private fun countQuerySize(
        since: Instant,
        until: Instant,
        resolution: Long,
    ): Long {
        val duration = Duration.between(since, until).toMinutes()
        return duration / resolution
    }

    private fun toSensor(sensorString: String): Sensor? {
        try {
            return Sensor.valueOf(sensorString.uppercase(Locale.getDefault()))
        } catch (e: IllegalArgumentException) {
            return null
        }
    }

}

data class GetMeasurementsResponse(
    val deviceId: String,
    val sensor: String,
    val resolution: Long,
    val measurements: List<TimeAndValue>,
)

enum class Sensor(
    val measurementName: String,
) {
    AIR_TEMP("air_temp"),
    HUMIDITY("humidity"),
    PRESSURE("pressure"),
    SOIL_TEMP("soil_temp"),
    RAIN("rain"),
    WIND("wind"),
}
