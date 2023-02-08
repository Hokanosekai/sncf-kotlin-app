package fr.hoka.sncf.entities

import android.os.Parcel
import android.os.Parcelable
import fr.hoka.sncf.enums.TrainType
import java.util.*

data class Train(
    val num: String, // code of the train
    val type: TrainType, // type of train
    val localHour: String, // local hours of the train
    val localMinute: String, // local minutes of the train
    val vehicle_journey_id: String // Vehicle Journeys of the train
) : Parcelable {

    var direction: String? = null // direction of the train
    var from: Stops? = null // source station of the train
    var to: Stops? = null // destination station of the train
    var stops: ArrayList<Stops> = arrayListOf() // list of stops of the train

    constructor(parcel: Parcel) : this(
        parcel.readString()!!, // num
        TrainType.valueOf(parcel.readString()!!), // type
        parcel.readString()!!, // localHour
        parcel.readString()!!, // localMinute
        parcel.readString()!! // vehicle_journey_id
    ) {
        from = parcel.readParcelable(Stops::class.java.classLoader)!!
        to = parcel.readParcelable(Stops::class.java.classLoader)!!
        parcel.readList(stops, Stops::class.java.classLoader)
    }

    /**
     * @param stop Stop to add
     * @param departureStation Boolean to set the source stop of the train
     * @param arrivalStation Boolean to set the destination stop of the train
     */
    fun addStops(stop: Stops, departureStation: Boolean, arrivalStation: Boolean) {
        stops.add(stop)
        if (departureStation) from = stop
        if (arrivalStation) to = stop
    }

    /**
     * @return A stringifies view of Train entity
     */
    override fun toString(): String {
        return "${from!!.getDeparture()} - $direction \n${type.name} - $num"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(num)
        parcel.writeString(type.name)
        parcel.writeString(localHour)
        parcel.writeString(localMinute)
        parcel.writeString(vehicle_journey_id)
        parcel.writeParcelable(from, flags)
        parcel.writeParcelable(to, flags)
        parcel.writeList(stops)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Train> {
        override fun createFromParcel(parcel: Parcel): Train {
            return Train(parcel)
        }

        override fun newArray(size: Int): Array<Train?> {
            return arrayOfNulls(size)
        }
    }
}
