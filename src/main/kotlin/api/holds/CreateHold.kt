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

@Path("/api/devices/{deviceId}/holds")
class CreateHold(
    private val holdDao: HoldDao,
    private val deviceDao: DeviceDao,
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
            return Response.status(422, "The label must be 8 characters.").build()
        }

        val device = deviceDao.find(deviceId)
        if (device == null) {
            return Response.status(404, "A device with id = $deviceId does not exist.").build()
        }

        val holdId = holdDao.create(body.label, body.start, null, deviceId)

        val startString = body.start.toString()
        val response = PostHoldResponse(holdId, body.label, startString, deviceId)
        return Response.status(201).entity(response).build()
    }

}

data class CreateHoldBody @JsonCreator constructor(
    @JsonProperty("label")
    val label: String,
    @JsonProperty("start")
    val start: Instant,
)

data class PostHoldResponse(
    val id: Int,
    val label: String,
    val start: String,
    val deviceId: Int,
)
