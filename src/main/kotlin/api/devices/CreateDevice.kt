package api.devices

import database.AccountDao
import database.Device
import database.DeviceDao
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/accounts/{accountId}/devices")
class CreateDevice(
    private val accountDao: AccountDao,
    private val deviceDao: DeviceDao,
) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun create(
        @PathParam("accountId")
        accountId: Int,
    ): Response {
        val account = accountDao.find(accountId)
        if (account == null) {
            return Response.status(404).build()
        }

        val deviceId = deviceDao.create(accountId)
        val device = Device(deviceId, accountId)

        return Response.status(201).entity(device).build()
    }

}
