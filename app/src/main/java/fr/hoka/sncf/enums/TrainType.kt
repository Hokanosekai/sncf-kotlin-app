package fr.hoka.sncf.enums

/**
 * Enum of the train types
 * @param name Name of the train type
 */
enum class TrainType(name: String) {
    TER("TER"),
    TGV("TGV"),
    INTERCITES("Intercites"),
    OUIGO("OUIGO");
}