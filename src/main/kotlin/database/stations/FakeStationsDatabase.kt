package database.stations

class FakeStationsDatabase {

    private val stations: MutableMap<String, String> = mutableMapOf()

    fun generateStations() {
        for (i in 1..9) {
            val label = "label-0$i"
            val imei = "imei-000000000$i"
            stations[label] = imei
        }
    }

    fun findLabel(labelParam: String): String? {
        val label = stations.keys.find { l -> l == labelParam }
        return label
    }

    fun findImeiByLabel(label: String): String? {
        val imei = stations[label]
        return imei
    }

}
