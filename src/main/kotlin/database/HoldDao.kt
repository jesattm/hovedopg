package database

import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlUpdate
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

}

data class Hold(
    val id: Int,
    val label: String,
    val start: Instant,
    val end: Instant?,
    val deviceId: Int,
)
