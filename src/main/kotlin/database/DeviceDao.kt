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

interface DeviceDao {

    @SqlUpdate("""
        INSERT INTO devices (id, account_id, timestamp)
        VALUES (:id, :accountId, :timestamp)
    """)
    fun create(
        @Bind("id") id: String,
        @Bind("accountId") accountId: String,
        @Bind("timestamp") timestamp: Instant?,
    )

    @SqlQuery("""
        SELECT id, account_id, timestamp 
        FROM devices
        WHERE id = :id
    """)
    @RegisterRowMapper(DeviceRowMapper::class)
    fun findById(
        @Bind("id") id: String
    ): Device?

    @SqlQuery("""
        SELECT id, account_id, timestamp 
        FROM devices
        WHERE account_id = :accountId
    """)
    @RegisterRowMapper(DeviceRowMapper::class)
    fun findByAccountId(
        @Bind("accountId") accountId: String
    ): List<Device>

}

class DeviceRowMapper : RowMapper<Device> {
    override fun map(r: ResultSet, ctx: StatementContext) = Device(
        r.getString("id"),
        r.getString("account_id"),
        r.getTimestamp("timestamp"),
    )
}

data class Device(
    val id: String,
    val accountId: String,
    val timestamp: Timestamp?
)
