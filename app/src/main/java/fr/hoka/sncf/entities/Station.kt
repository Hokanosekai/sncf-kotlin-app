package fr.hoka.sncf.entities

import android.os.Parcel
import android.os.Parcelable
import fr.hoka.sncf.enums.TrainType

class Station(
    val code_uic: Int, // UIC code of the station
    val lib: String, // Station label or name
    val long: Double, // Longitude of the station
    val lat: Double, // Latitude of the station
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() as String,
        parcel.readDouble(),
        parcel.readDouble()
    )

    /**
     * @return A stringifies view of Station entity
     */
    override fun toString(): String {
        // Here we only return the station name
        // Nobody search a station by it's UIC code
        return this.lib
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeInt(code_uic)
        p0.writeString(lib)
        p0.writeDouble(long)
        p0.writeDouble(lat)
    }

    companion object CREATOR : Parcelable.Creator<Station> {
        override fun createFromParcel(parcel: Parcel): Station {
            return Station(parcel)
        }

        override fun newArray(size: Int): Array<Station?> {
            return arrayOfNulls(size)
        }
    }
}