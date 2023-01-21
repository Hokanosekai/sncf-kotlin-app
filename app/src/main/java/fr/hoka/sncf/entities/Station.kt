package fr.hoka.sncf.entities

class Station(
    val code_uic: Int, // UIC code of the station
    val lib: String, // Station label or name
    val long: Double, // Longitude of the station
    val lat: Double, // Latitude of the station
) {

    /**
     * @return A stringified view of Station entity
     */
    override fun toString(): String {
        // Here we only return the station name
        // Nobody search a station by it's UIC code
        return this.lib;
    }
}