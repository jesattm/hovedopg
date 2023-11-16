package api.holds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import database.DeviceDao
import database.HoldDao
import java.time.Instant
import javax.validation.constraints.NotNull
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/devices/{deviceId}/release")
class ReleaseHold(
    private val deviceDao: DeviceDao,
    private val holdDao: HoldDao,
    private val finder: ActiveHoldFinder,
) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun release(
        @PathParam("deviceId")
        deviceId: Int,
        @NotNull
        body: ReleaseHoldBody,
        ): Response {
        val device = deviceDao.findById(deviceId)
        if (device == null) {
            return Response.status(404, "Device not found.").build()
        }

        val holds = holdDao.findByDevice(deviceId)
        if (holds.isEmpty()) {
            return Response.status(404, "Device has no holds.").build()
        }

        val activeHold = finder.find(holds)
        if (activeHold == null) {
            return Response.status(409, "There is no active hold on the device.").build()
        }

        val startInstant = activeHold.start.toInstant()
        if (body.end <= startInstant) {
            return Response.status(422, "End must be after start.").build()
        }

        holdDao.setEnd(activeHold.id, body.end)

        return Response.status(204).build()
    }

}

data class ReleaseHoldBody @JsonCreator constructor(
    @JsonProperty("end")
    val end: Instant
)
