package api.holds

import database.DeviceDao
import database.Hold
import database.HoldDao
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/devices/{deviceId}/holds")
class GetHoldsByDeviceId(
    private val deviceDao: DeviceDao,
    private val holdDao: HoldDao,
) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun get(
        @PathParam("deviceId")
        deviceId: Int
    ): Response {
        val account = deviceDao.findById(deviceId)
        if (account == null) {
            return Response.status(404).build()
        }

        val holds = holdDao.findByDevice(deviceId)

        val result = holds.map { toResponse(it) }
        return Response.status(200).entity(result).build()
    }

    private fun toResponse(hold: Hold) = HoldResponse(
        hold.id,
        hold.label,
        hold.start.toString(),
        hold.end?.toString(),
        hold.deviceId,
    )

}

data class HoldResponse(
    val id: Int,
    val label: String,
    val start: String,
    val end: String?,
    val deviceId: Int,
)
