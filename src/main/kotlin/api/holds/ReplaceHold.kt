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

@Path("/api/devices/{deviceId}/replace")
class ReplaceHold(
    private val labels: FakeLabelsDatabase,
    private val checker: ActiveLabelChecker,
    private val deviceDao: DeviceDao,
    private val holdDao: HoldDao,
    private val holdFinder: ActiveHoldFinder,
) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun replace(
        @PathParam("deviceId")
        deviceId: String,
        @NotNull
        body: ReplaceHoldBody,
    ): Response {
        val device = deviceDao.findById(deviceId)
        if (device == null) {
            return Response.status(404, "Device not found.").build()
        }

        val holds = holdDao.findByDevice(deviceId)
        if (holds.isEmpty()) {
            return Response.status(404, "Device has no holds.").build()
        }

        val activeHold = holdFinder.find(holds)
        if (activeHold == null) {
            return Response.status(409, "There is no active hold on the device.").build()
        }

        val startInstant = activeHold.start.toInstant()
        if (body.retiredSince <= startInstant) {
            return Response
                .status(422, "retiredSince must be after the hold's start.")
                .build()
        }

        if (body.replacementLabel.length != 8) {
            return Response.status(422, "Label must have 8 characters.").build()
        }

        val label = labels.find(body.replacementLabel)
        if (label == null) {
            return Response.status(404, "Label not found.").build()
        }

        val activeLabel = checker.check(body.replacementLabel)
        if (activeLabel) {
            return Response.status(409, "Label in use.").build()
        }

        if (body.claimedSince <= body.retiredSince) {
            return Response
                .status(422, "claimedSince must be after retiredSince.")
                .build()
        }

        holdDao.setEnd(activeHold.id, body.retiredSince)
        val holdId = holdDao.create(deviceId, body.replacementLabel, body.imei, body.claimedSince, null)

        val response = ReplaceHoldResponse(holdId)
        return Response.status(201).entity(response).build()
    }

}

data class ReplaceHoldBody @JsonCreator constructor(
    @JsonProperty("retiredSince")
    val retiredSince: Instant,
    @JsonProperty("replacementLabel")
    val replacementLabel: String,
    @JsonProperty("imei")
    val imei: String?,
    @JsonProperty("claimedSince")
    val claimedSince: Instant,
)

data class ReplaceHoldResponse(
    val replacementHoldId: Int,
)
