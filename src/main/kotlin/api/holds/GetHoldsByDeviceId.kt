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
        deviceId: String
    ): Response {
        val device = deviceDao.findById(deviceId)
        if (device == null) {
            return Response.status(404).build()
        }

        val holds = holdDao.findByDevice(deviceId)

        val result = holds.map { toResponse(it) }
        return Response.status(200).entity(result).build()
    }

    private fun toResponse(hold: Hold) = HoldResponse(
        hold.id,
        hold.deviceId,
        hold.label,
        hold.imei,
        hold.start.toString(),
        hold.end?.toString(),
        hold.timestamp?.toString(),
    )

}

data class HoldResponse(
    val id: Int,
    val deviceId: String,
    val label: String,
    val imei: String?,
    val start: String,
    val end: String?,
    val timestamp: String?,
)
