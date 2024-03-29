package api.holds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import database.DeviceDao
import database.HoldDao
import database.stations.FakeStationsDatabase
import java.time.Instant
import javax.validation.constraints.NotNull
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/devices/{deviceId}/holds")
class CreateHold(
    private val stations: FakeStationsDatabase,
    private val checker: ActiveLabelChecker,
    private val deviceDao: DeviceDao,
    private val holdDao: HoldDao,
    private val holdFinder: ActiveHoldFinder,
    private val endFinder: LatestEndFinder,
) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun create(
        @PathParam("deviceId")
        deviceId: String,
        @NotNull
        body: CreateHoldBody,
    ): Response {
        if (body.label.length != 8) {
            return Response.status(422, "Label must have 8 characters.").build()
        }

        val label = stations.findLabel(body.label)
        if (label == null) {
            return Response.status(404, "Label not found.").build()
        }

        val activeLabel = checker.check(body.label)
        if (activeLabel) {
            return Response.status(409, "Label in use.").build()
        }

        val device = deviceDao.findById(deviceId)
        if (device == null) {
            return Response.status(404, "Device not found.").build()
        }

        val deviceHolds = holdDao.findByDevice(deviceId)

        val activeHold = holdFinder.find(deviceHolds)
        if (activeHold != null) {
            return Response.status(409, "Device has active hold.").build()
        }

        val latestEnd = endFinder.find(deviceHolds)
        if (latestEnd != null) {
            if (body.start <= latestEnd) {
                return Response
                    .status(422, "Start must be after previous hold's end.")
                    .build()
            }
        }

        val imei = stations.findImeiByLabel(body.label)
        if (imei == null) {
            return Response.status(404, "Imei not found.").build()
        }

        val id = holdDao.create(deviceId, body.label, imei, body.start, null)

        val response = CreateHoldResponse(id)
        return Response.status(201).entity(response).build()
    }

}

data class CreateHoldBody @JsonCreator constructor(
    @JsonProperty("label")
    val label: String,
    @JsonProperty("start")
    val start: Instant,
)

data class CreateHoldResponse(
    val id: Int
)
