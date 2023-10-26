package database

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.sql.ResultSet

interface AccountDao {

    @SqlUpdate("""
        INSERT INTO accounts
        VALUES ()
    """)
    @GetGeneratedKeys
    fun create(): Int

    @SqlQuery("""
        SELECT id
        FROM accounts
        WHERE id = :id
    """)
    @RegisterRowMapper(AccountRowMapper::class)
    fun find(@Bind("id") id: Int): Account?

}

class AccountRowMapper : RowMapper<Account> {
    override fun map(r: ResultSet, ctx: StatementContext) = Account(
        r.getInt("id"),
    )
}

data class Account(
    val id: Int,
)
