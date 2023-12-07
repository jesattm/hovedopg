package api.holds

import database.HoldDao
import javax.ws.rs.DELETE
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Response

@Path("/api/holds/{holdId}")
class DeleteHold(
    private val dao: HoldDao,
) {

    @DELETE
    fun delete(
        @PathParam("holdId")
        id: Int,
    ): Response {
        val hold = dao.findById(id)
        if (hold == null) {
            return Response.status(404, "Hold not found.").build()
        }

        dao.delete(id)

        return Response.status(204).build()
    }

}
