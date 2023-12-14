package api.accounts

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import database.AccountDao
import javax.validation.constraints.NotNull
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/accounts")
class CreateAccount(
    private val dao: AccountDao,
) {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun create(
        @NotNull
        body: CreateAccountBody,
    ): Response {
        val account = dao.find(body.id)
        if (account != null) {
            return Response.status(409, "Account id already exists.").build()
        }

        dao.create(body.id, body.apiKey)

        return Response.status(204).build()
    }

}

data class CreateAccountBody @JsonCreator constructor(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("apiKey")
    val apiKey: String,
)
