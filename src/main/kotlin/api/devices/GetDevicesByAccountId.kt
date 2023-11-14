package api.devices

import database.AccountDao
import database.DeviceDao
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/accounts/{accountId}/devices")
class GetDevicesByAccountId(
    private val accountDao: AccountDao,
    private val deviceDao: DeviceDao,
) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun get(
        @PathParam("accountId")
        accountId: Int
    ): Response {
        val account = accountDao.find(accountId)
        if (account == null) {
            return Response.status(404).build()
        }

        val devices = deviceDao.findByAccountId(accountId)

        return Response.status(200).entity(devices).build()
    }

}
