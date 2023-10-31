package database

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.sql.ResultSet

interface DeviceDao {

    @SqlUpdate("""
        INSERT INTO devices (account_id)
        VALUES (:accountId)
    """)
    @GetGeneratedKeys
    fun create(
        @Bind("accountId") accountId: Int
    ): Int

    @SqlQuery("""
        SELECT id, account_id
        FROM devices
        WHERE id = :id
    """)
    @RegisterRowMapper(DeviceRowMapper::class)
    fun find(
        @Bind("id") id: Int
    ): Device?

}

class DeviceRowMapper : RowMapper<Device> {
    override fun map(r: ResultSet, ctx: StatementContext) = Device(
        r.getInt("id"),
        r.getInt("account_id"),
    )
}

data class Device(
    val id: Int,
    val accountId: Int,
)
