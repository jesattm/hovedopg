package database.labels

class FakeLabelsDatabase {

    private val labels: ArrayList<String> = arrayListOf()

    fun generateLabels() {
        for (i in 1..9) {
            val label = "QWER000$i"
            labels.add(label)
        }
    }

    fun find(
        labelParam: String,
    ): String? {
        val label = labels.find { l -> l == labelParam }
        return label
    }

}
