package api.claim

import database.Claim
import database.ClaimDao
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/accounts/{accountId}/claims")
class PostClaim(
    private val dao: ClaimDao,
) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun create(
        @PathParam("accountId")
        accountId: Int,
    ): Response {
        val claimId = dao.create(accountId)
        val claim = Claim(claimId, accountId)

        return Response.status(201).entity(claim).build()
    }

}
