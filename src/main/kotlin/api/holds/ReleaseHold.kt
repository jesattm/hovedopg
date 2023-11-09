package api.holds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import database.HoldDao
import java.time.Instant
import javax.validation.constraints.NotNull
import javax.ws.rs.Consumes
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/holds/{id}")
class ReleaseHold(
    private val dao: HoldDao
) {

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun release(
        @PathParam("id")
        id: Int,
        @NotNull
        body: ReleaseHoldBody,
        ): Response {
        val hold = dao.findById(id)
        if (hold == null) {
            return Response.status(404, "Hold not found.").build()
        }

        val startInstant = hold.start.toInstant()
        if (body.end <= startInstant) {
            return Response.status(422, "End must be after start.").build()
        }

        dao.setEnd(id, body.end)

        return Response.status(204).build()
    }

}

data class ReleaseHoldBody @JsonCreator constructor(
    @JsonProperty("end")
    val end: Instant
)