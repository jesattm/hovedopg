package migration

import database.DeviceDao
import database.HoldDao
import java.time.Instant

class MigrateDevices(
    private val deviceDao: DeviceDao,
    private val holdDao: HoldDao,
) {

    fun migrate(
        events: List<DeviceEvent>,
        removedAccounts: List<String>,
        ) {
        val filtered = filterDevices(events, removedAccounts)

        val sorted = filtered.map { list -> list.sortedBy { device -> device.eventId } }
        var count = 1
        var lastHoldId = 0
        sorted.forEach { list ->
            println("---------------- Count = $count -----------------")
            count += 1
            list.forEach { event ->
                if (event.eventId == 1) {
                    deviceDao.create(event.deviceId, event.orgId)
                    val claimedAtInstant = Instant.parse(event.claimedAt)
                    lastHoldId = holdDao.create(event.deviceId, event.label, event.imei, claimedAtInstant, null)
                    println("Event id 1 created, lastHoldId = $lastHoldId")
                }
                else {
                    if (event.type == "DEVICE_CLAIMED") {
                        val claimedAtInstant = Instant.parse(event.claimedAt)
                        lastHoldId = holdDao.create(event.deviceId, event.label, event.imei, claimedAtInstant, null)
                        println("Event id over 1 created, lastHoldId = $lastHoldId")
                    }
                    else if (event.type == "DEVICE_RETIRED") {
                        val retiredAtInstant = Instant.parse(event.retiredAt)
                        holdDao.setEnd(lastHoldId, retiredAtInstant)
                        println("Event id over 1 retired, lastHoldId = $lastHoldId")
                    }
                    else {
                        println("Unknown event type '${event.type}'")
                    }
                }
            }
        }
    }

    private fun filterDevices(
        events: List<DeviceEvent>,
        removedAccounts: List<String>,
    ): List<List<DeviceEvent>> {
        val grouped = groupByDeviceId(events)
        val sorted = sortByEventId(grouped)
        println("Size before removing dropped devices: ${sorted.size}")
        val filtered1 = removeDroppedDevices(sorted)
        println("Size after removing dropped devices: ${filtered1.size}")
        val filtered2 = removeDeletedAccounts(filtered1, removedAccounts)
        println("Size after removing devices with deleted accounts: ${filtered2.size}")
        return filtered2
    }


    private fun groupByDeviceId(events: List<DeviceEvent>): List<List<DeviceEvent>> {
        val grouped = events.groupBy { it.deviceId }
        val list = grouped.values.toList()
        return list
    }

    private fun sortByEventId(events: List<List<DeviceEvent>>): List<List<DeviceEvent>> {
        val sorted = events.map { list -> list.sortedByDescending { device -> device.eventId } }
        return sorted
    }

    private fun removeDroppedDevices(events: List<List<DeviceEvent>>): List<List<DeviceEvent>> {
        val filtered = events.filterNot { list -> list[0].type == "DEVICE_DROPPED" }
        return filtered
    }

    private fun removeDeletedAccounts(
        events: List<List<DeviceEvent>>,
        removedAccounts: List<String>,
    ): List<List<DeviceEvent>> {
        val filtered = events.filterNot { list -> removedAccounts.contains(list[0].orgId) }
        return filtered
    }
}
