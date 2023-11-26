package api.accounts

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import database.AccountDao
import java.time.Instant
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
        dao.create(body.id, body.apiKey, body.timestamp)

        return Response.status(204).build()
    }

}

data class CreateAccountBody @JsonCreator constructor(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("apiKey")
    val apiKey: String,
    @JsonProperty("timestamp")
    val timestamp: Instant?,
)
