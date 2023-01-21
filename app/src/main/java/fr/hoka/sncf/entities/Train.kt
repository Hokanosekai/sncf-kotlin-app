package fr.hoka.sncf.entities

class Train (
    private val num: Int, // code of the train
    private val dest: String, // destination station of the train
    private val prov: String, // source station of the train
    private val h: Int, // hours of departure date time
    private val m: Int, // minutes of departure date time
        ) {

    /**
     * @return A stringified view of Train entity
     */
    override fun toString(): String {
        // Add the 0 before hours and minutes if there is less than 10
        // Example:
        // h = 6, m = 7
        // without `padStart` >> 6h7
        // with `padStart` >> 06h07
        val time = "${h.toString().padStart(2, '0')}h${m.toString().padStart(2, '0')}"
        return "$time - $dest : $num"
    }
}