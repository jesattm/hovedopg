package api.holds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import database.DeviceDao
import database.HoldDao
import database.labels.FakeLabelsDatabase
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
    private val holdDao: HoldDao,
    private val labels: FakeLabelsDatabase,
    private val deviceDao: DeviceDao,
    private val checker: ActiveLabelChecker,
    private val finder: LastestEndFinder,
) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun create(
        @PathParam("deviceId")
        deviceId: Int,
        @NotNull
        body: CreateHoldBody,
    ): Response {
        if (body.label.length != 8) {
            return Response.status(422, "Label must have 8 characters.").build()
        }

        val label = labels.find(body.label)
        if (label == null) {
            return Response.status(404, "Label not found.").build()
        }

        val isActive = checker.check(body.label)
        if (isActive) {
            return Response.status(409, "Label in use.").build()
        }

        val device = deviceDao.find(deviceId)
        if (device == null) {
            return Response.status(404, "Device not found.").build()
        }

        val deviceHolds = holdDao.findByDevice(deviceId)

        val activeHold = deviceHolds.find { it.end == null }
        if (activeHold != null) {
            return Response.status(409, "Device has active hold.").build()
        }

        val latestEnd = finder.find(deviceHolds)
        if (latestEnd != null) {
            if (body.start <= latestEnd) {
                return Response
                    .status(422, "Start must be after previous hold's end.")
                    .build()
            }
        }

        val holdId = holdDao.create(body.label, body.start, null, deviceId)

        val startString = body.start.toString()
        val response = CreateHoldResponse(holdId, body.label, startString, deviceId)
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
    val id: Int,
    val label: String,
    val start: String,
    val deviceId: Int,
)
