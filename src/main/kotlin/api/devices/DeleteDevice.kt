package api.devices

import database.DeviceDao
import javax.ws.rs.DELETE
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Response

@Path("/api/devices/{deviceId}")
class DeleteDevice(
    private val dao: DeviceDao,
) {

    @DELETE
    fun delete(
        @PathParam("deviceId")
        id: String
    ): Response {
        val device = dao.findById(id)
        if (device == null) {
            return Response.status(404, "Device not found.").build()
        }

        dao.delete(id)

        return Response.status(204).build()
    }

}
