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
        INSERT INTO holds (label, start, end, device_id)
        VALUES (:label, :start, :end, :deviceId)
    """)
    @GetGeneratedKeys
    fun create(
        @Bind("label") label: String,
        @Bind("start") start: Instant,
        @Bind("end") end: Instant?,
        @Bind("deviceId") deviceId: Int,
    ): Int

    @SqlQuery("""
        SELECT id, label, start, end, device_id
        FROM holds
        WHERE label = :label
    """)
    @RegisterRowMapper(HoldRowMapper::class)
    fun findByLabel(
        @Bind("label") label: String
    ): List<Hold>

    @SqlQuery("""
        SELECT id, label, start, end, device_id
        FROM holds
        WHERE device_id = :deviceId
    """)
    @RegisterRowMapper(HoldRowMapper::class)
    fun findByDevice(
        @Bind("deviceId") deviceId: Int
    ): Hold?

    @SqlUpdate("""
        UPDATE holds 
        SET end = :end
        WHERE id = :id
    """)
    fun setEnd(
        @Bind("id") id: Int,
        @Bind("end") end: Instant,
    )

}

class HoldRowMapper : RowMapper<Hold> {
    override fun map(r: ResultSet, ctx: StatementContext) = Hold(
        r.getInt("id"),
        r.getString("label"),
        r.getTimestamp("start"),
        r.getTimestamp("end"),
        r.getInt("device_id"),
    )
}

data class Hold(
    val id: Int,
    val label: String,
    val start: Timestamp,
    val end: Timestamp?,
    val deviceId: Int,
)
