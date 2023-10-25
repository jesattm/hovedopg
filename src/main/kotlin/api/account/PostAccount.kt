package api.account

import database.Account
import database.AccountDao
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/accounts")
class PostAccount(
    private val dao: AccountDao,
) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun create(): Response {
        val id = dao.create()
        val account = Account(id)

        return Response.status(201).entity(account).build()
    }

}
