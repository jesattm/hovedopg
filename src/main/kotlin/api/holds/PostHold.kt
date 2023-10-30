package api.holds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import database.HoldDao
import java.time.Instant
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/devices/{deviceId}/holds")
class PostHold(
    private val dao: HoldDao,
) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun create(
        @PathParam("deviceId")
        deviceId: Int,
        body: CreateHoldBody,
    ): Response {
        val start = Instant.parse(body.start)
        val holdId = dao.create(body.label, start, null, deviceId)

        val response = PostHoldResponse(holdId.toString(), body.label, body.start, null, deviceId.toString())
        return Response.status(201).entity(response).build()
    }

}

data class CreateHoldBody @JsonCreator constructor(
    @JsonProperty("label") val label: String,
    @JsonProperty("start") val start: String,
)

data class PostHoldResponse(
    val id: String,
    val label: String,
    val start: String,
    val end: String?,
    val deviceId: String,
)
