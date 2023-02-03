package fr.hoka.sncf.entities

import android.os.Parcel
import android.os.Parcelable

data class Stops (
    private val hourArrival: String,
    private val minuteArrival: String,
    private val hourDeparture: String,
    private val minuteDeparture: String
        ): Parcelable {

    private lateinit var station: Station;

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
        station = parcel.readParcelable(Station::class.java.classLoader)!!
    }

    fun setStation(station: Station) {
        this.station = station
    }

    fun getStation(): Station {
        return this.station
    }

    fun getDeparture(): String {
        return "${hourDeparture.padStart(2, '0')}h${minuteDeparture.padStart(2, '0')}";
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(hourArrival)
        p0.writeString(minuteArrival)
        p0.writeString(hourDeparture)
        p0.writeString(minuteDeparture)
        p0.writeParcelable(station, p1)
    }

    companion object CREATOR : Parcelable.Creator<Stops> {
        override fun createFromParcel(parcel: Parcel): Stops {
            return Stops(parcel)
        }

        override fun newArray(size: Int): Array<Stops?> {
            return arrayOfNulls(size)
        }
    }
}