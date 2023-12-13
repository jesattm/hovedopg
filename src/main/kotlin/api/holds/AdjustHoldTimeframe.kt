package api.holds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
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

@Path("/api/holds/{holdId}/adjust-timeframe")
class AdjustHoldTimeframe(
    private val dao: HoldDao,
) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun adjust(
        @PathParam("holdId")
        holdId: Int,
        @NotNull
        body: AdjustHoldTimeframeBody,
    ): Response {
        val hold = dao.findById(holdId)
        if (hold == null) {
            return Response.status(404, "Hold not found.").build()
        }

        if (body.start == null && body.end == null) {
            return Response.status(200,
                "Nothing to modify: Both start and end are null.").build()
        }

        if (body.start != null && body.end != null) {
            if (!body.start.isBefore(body.end)) {
                return Response.status(422,
                    "Start parameter must be before end parameter.").build()
            }
        }

        val holds = dao.findByDevice(hold.deviceId).sortedBy { it.start }
        val index = holds.indexOf(hold)
        if (body.start != null) {
            val holdEnd = hold.end?.toInstant()
            if (holdEnd != null && !body.start.isBefore(holdEnd)) {
                return Response.status(409,
                    "Start parameter must be before the hold's end.").build()
            }

            if (index != 0) {
                val previousEnd = holds[index-1].end?.toInstant()
                if (previousEnd == null) {
                    return Response.status(400, "Critical internal error: " +
                            "Previous hold's 'end' is null.").build()
                }
                if (!body.start.isAfter(previousEnd)) {
                    return Response.status(409,
                        "Start parameter overlaps previous hold.").build()
                }
            }
        }

        if (body.end != null) {
            if (hold.end == null) {
                return Response.status(422,
                    "Setting an active hold's end is not allowed here.").build()
            }

            val holdStart = hold.start.toInstant()
            if (!body.end.isAfter(holdStart)) {
                return Response.status(409,
                    "End parameter must be after the hold's start.").build()
            }

            if (index != holds.lastIndex) {
                val nextStart = holds[index+1].start.toInstant()
                if (!body.end.isBefore(nextStart)) {
                    return Response.status(409,
                        "End parameter overlaps next hold.").build()
                }
            }
        }

        dao.update(holdId, body.start, body.end)

        return Response.status(204).build()
    }

}

data class AdjustHoldTimeframeBody @JsonCreator constructor(
    @JsonProperty("start")
    val start: Instant?,
    @JsonProperty("end")
    val end: Instant?,
)
