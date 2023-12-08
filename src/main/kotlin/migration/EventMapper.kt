package migration

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

class EventMapper {

    fun map(): EventsMapperLists {
        val objectMapper = ObjectMapper()

        val deviceEvents: ArrayList<DeviceEvent> = arrayListOf()
        val accountEvents: ArrayList<AccountEvent> = arrayListOf()

        try {
            File("src/main/resources/events.jsonl").forEachLine { line ->
                val jsonNode = objectMapper.readTree(line)
                val type = jsonNode.get("type").asText()

                if (type.substring(0, 6) == "DEVICE") {
                    val event: DeviceEvent = objectMapper.readValue(line, DeviceEvent::class.java)
                    deviceEvents.add(event)
                }
                else if (type.substring(0, 7) == "ACCOUNT") {
                    val event: AccountEvent = objectMapper.readValue(line, AccountEvent::class.java)
                    accountEvents.add(event)
                }
                else {
                    println("$line \n The above json cannot be identified. \n\n")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val lists = EventsMapperLists(deviceEvents, accountEvents)
        return lists
    }

}

data class DeviceEvent @JsonCreator constructor(
    @JsonProperty("type")
    val type: String,
    @JsonProperty("streamId")
    val streamId: String,
    @JsonProperty("eventId")
    val eventId: Int,
    @JsonProperty("orgId")
    val orgId: String,
    @JsonProperty("deviceId")
    val deviceId: String,
    @JsonProperty("label")
    val label: String,
    @JsonProperty("imei")
    val imei: String,
    @JsonProperty("claimedAt")
    val claimedAt: String?,
    @JsonProperty("retiredAt")
    val retiredAt: String?,
    @JsonProperty("timestamp")
    val timestamp: String,
)

data class AccountEvent @JsonCreator constructor(
    @JsonProperty("type")
    val type: String,
    @JsonProperty("streamId")
    val streamId: String,
    @JsonProperty("eventId")
    val eventId: Int,
    @JsonProperty("accountId")
    val accountId: String,
    @JsonProperty("apiKey")
    val apiKey: String?,
    @JsonProperty("hashword")
    val hashword: String?,
    @JsonProperty("timestamp")
    val timestamp: String,
)

data class EventsMapperLists(
    val deviceEvents: List<DeviceEvent>,
    val accountEvents: List<AccountEvent>,
)
