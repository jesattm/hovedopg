package database

import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface AccountDao {

    @SqlUpdate("""
        INSERT INTO accounts
        VALUES ()
    """)
    @GetGeneratedKeys
    fun create(): Int

}

data class Account(
    val id: Int,
)
