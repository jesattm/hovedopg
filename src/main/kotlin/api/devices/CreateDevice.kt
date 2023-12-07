package api.devices

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import database.AccountDao
import database.DeviceDao
import javax.validation.constraints.NotNull
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/accounts/{accountId}/devices")
class CreateDevice(
    private val accountDao: AccountDao,
    private val deviceDao: DeviceDao,
) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun create(
        @PathParam("accountId")
        accountId: String,
        @NotNull
        body: CreateDeviceBody,
    ): Response {
        val account = accountDao.find(accountId)
        if (account == null) {
            return Response.status(404, "Account not found.").build()
        }

        deviceDao.create(body.id, accountId)

        return Response.status(204).build()
    }

}

data class CreateDeviceBody @JsonCreator constructor(
    @JsonProperty("id")
    val id: String,
)
