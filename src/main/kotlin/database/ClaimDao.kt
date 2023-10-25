package database

import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface ClaimDao {

    @SqlUpdate("""
        INSERT INTO claims (account_id)
        VALUES (:accountId)
    """)
    @GetGeneratedKeys
    fun create(
        @Bind("accountId") accountId: Int
    ): Int

}

data class Claim(
    val id: Int,
    val accountId: Int,
)
