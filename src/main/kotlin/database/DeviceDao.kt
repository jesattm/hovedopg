package database

import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface DeviceDao {

    @SqlUpdate("""
        INSERT INTO devices (account_id)
        VALUES (:accountId)
    """)
    @GetGeneratedKeys
    fun create(
        @Bind("accountId") accountId: Int
    ): Int

}

data class Device(
    val id: Int,
    val accountId: Int,
)
