package database

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

interface HoldDao {

    @SqlUpdate("""
        INSERT INTO holds (device_id, label, imei, start, end)
        VALUES (:deviceId, :label, :imei, :start, :end)
    """)
    @GetGeneratedKeys
    fun create(
        @Bind("deviceId") deviceId: String,
        @Bind("label") label: String,
        @Bind("imei") imei: String?,
        @Bind("start") start: Instant,
        @Bind("end") end: Instant?,
    ): Int

    @SqlUpdate("""
        UPDATE holds 
        SET end = :end
        WHERE id = :id
    """)
    fun setEnd(
        @Bind("id") id: Int,
        @Bind("end") end: Instant,
    )

    @SqlQuery("""
        SELECT id, device_id, label, imei, start, end, timestamp
        FROM holds
        WHERE label = :label
    """)
    @RegisterRowMapper(HoldRowMapper::class)
    fun findByLabel(
        @Bind("label") label: String
    ): List<Hold>

    @SqlQuery("""
        SELECT id, device_id, label, imei, start, end, timestamp
        FROM holds
        WHERE id = :id
    """)
    @RegisterRowMapper(HoldRowMapper::class)
    fun findById(
        @Bind("id") id: Int
    ): Hold?

    @SqlQuery("""
        SELECT id, device_id, label, imei, start, end, timestamp
        FROM holds
        WHERE device_id = :deviceId
    """)
    @RegisterRowMapper(HoldRowMapper::class)
    fun findByDevice(
        @Bind("deviceId") deviceId: String
    ): List<Hold>

}

class HoldRowMapper : RowMapper<Hold> {
    override fun map(r: ResultSet, ctx: StatementContext) = Hold(
        r.getInt("id"),
        r.getString("device_id"),
        r.getString("label"),
        r.getString("imei"),
        r.getTimestamp("start"),
        r.getTimestamp("end"),
    )
}

data class Hold(
    val id: Int,
    val deviceId: String,
    val label: String,
    val imei: String?,
    val start: Timestamp,
    val end: Timestamp?,
)
