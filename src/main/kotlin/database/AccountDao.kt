package database

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

interface AccountDao {

    @SqlUpdate("""
        INSERT INTO accounts (id, api_key, timestamp)
        VALUES (:id, :apiKey, :timestamp)
    """)
    fun create(
        @Bind("id") id: String,
        @Bind("apiKey") apiKey: String,
        @Bind("timestamp") timestamp: Instant?,
    )

    @SqlQuery("""
        SELECT id, api_key, timestamp
        FROM accounts
        WHERE id = :id
    """)
    @RegisterRowMapper(AccountRowMapper::class)
    fun find(
        @Bind("id") id: String
    ): Account?

}

class AccountRowMapper : RowMapper<Account> {
    override fun map(r: ResultSet, ctx: StatementContext) = Account(
        r.getString("id"),
        r.getString("api_key"),
        r.getTimestamp("timestamp"),
    )
}

data class Account(
    val id: String,
    val apiKey: String,
    val timestamp: Timestamp?,
)
