package api.accounts

import database.AccountDao
import javax.ws.rs.DELETE
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Response

@Path("/api/accounts/{accountId}")
class DeleteAccount(
    private val dao: AccountDao,
) {

    @DELETE
    fun delete(
        @PathParam("accountId")
        id: String
    ): Response {
        val account = dao.find(id)
        if (account == null) {
            return Response.status(404, "Account not found.").build()
        }

        dao.delete(id)

        return Response.status(204).build()
    }

}
