package fr.hoka.sncf.services

import fr.hoka.sncf.entities.Station
import fr.hoka.sncf.entities.Stops
import fr.hoka.sncf.entities.Train
import fr.hoka.sncf.enums.TrainType
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ApiSNCF(
    private var baseUri: String, // Base uri of the API
    private var token: String // Our API_KEY
) {
    // Instantiate the HttpClient
    private var client: OkHttpClient = OkHttpClient()

    /**
     * Fetch from the SNCF Api the 8 first departures of a Station,
     * and return it to a List of Train.
     * @param codeUIC The Station code.
     * @param limit Number of results
     * @param cb Callback object
     * @return The 8 first Train.
     */
    fun fetchDeparturesFromStopArea(codeUIC: Int, limit: Int = 8, cb: Callback) {
        // Build the uri
        val uri =
            "$baseUri/coverage/sncf/stop_areas/stop_area:SNCF:$codeUIC/departures?key=$token&count=$limit"

        // Build the request
        val request = Request.Builder()
            .url(uri)
            .build()

        // Send the GET request to the Api
        client.newCall(request).enqueue(cb)
    }

    /**
     * Fetch from the SNCF Api the list of stops of a Train,
     * and add it to the Train entity.
     * @param vehicleJourneyId The Train journey id.
     * @param cb Callback object
     * @return The Train with the list of stops.
     */
    fun fetchVehicleJourney(vehicleJourneyId: String, cb: Callback) {
        // Build the uri
        val uri =
            "$baseUri/coverage/sncf/vehicle_journeys/$vehicleJourneyId/vehicle_journeys?key=$token"

        // Build the request
        val request = Request.Builder()
            .url(uri)
            .build()

        // Send the GET request to the Api
        client.newCall(request).enqueue(cb)
    }

    /**
     * Parse fetched JSON to create a List of Train entity.
     * @param json The body of the response.
     * @return The parsed List of Train.
     */
    fun parseJSONDepartures(json: ResponseBody, station: Station): List<Train> {
        val tmpTrains: ArrayList<Train> = ArrayList()
        try {
            val obj = JSONObject(json.string())
            // Parse as JSONArray departures
            val departures: JSONArray = obj.getJSONArray("departures")

            // Iterate through departures
            for (i in 0 until departures.length()) {
                // train object
                val trainObj = departures.getJSONObject(i)

                // display_informations and stop_date_time
                val trainInfo = trainObj.getJSONObject("display_informations")
                val trainStopDateTime = trainObj.getJSONObject("stop_date_time")
                val links = trainObj.getJSONArray("links")

                // Train Code, Train Destination, Train Source
                val direction: String = trainInfo.getString("direction")
                val num: String = trainInfo.getString("trip_short_name")
                val type: String = trainInfo.getString("commercial_mode").toString().split(" ")[0]

                // The train departure date time
                val timestamp = trainStopDateTime.getString("departure_date_time")
                // split it to get only hours and minutes then split it again because we don't want seconds
                val h: String = timestamp.split("T").last().chunked(2)[0]
                val m: String = timestamp.split("T").last().chunked(2)[1]

                // Vehicle journey from links
                val vehicleJourney: JSONObject = links[1] as JSONObject
                val vehicleJourneyId: String = vehicleJourney.getString("id")

                // Create the stop then set the station
                val stopFrom = Stops(h, m, h, m)
                stopFrom.setStation(station)

                // Get the train type
                val t: TrainType = try {
                    TrainType.valueOf(type)
                } catch (e: java.lang.IllegalArgumentException) {
                    TrainType.TER
                }

                // Create the train
                val train = Train(
                    num,
                    t, h, m, vehicleJourneyId)
                train.from = stopFrom
                train.direction = direction

                // Add the new train to the tmp list
                tmpTrains.add(train)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        // Return the list of trains
        return tmpTrains.toList()
    }

    fun parseJSONVehicleJourneys(train: Train, json: ResponseBody): Train {
        val obj = JSONObject(json.string())
        val vehicleJourneyObj = obj.getJSONArray("vehicle_journeys")[0] as JSONObject

        val stops = vehicleJourneyObj.getJSONArray("stop_times")

        for (i in 0 until stops.length()) {
            val stopObj = stops.getJSONObject(i)

            val hArrival = stopObj.getString("arrival_time").toString().chunked(2)[0]
            val mArrival = stopObj.getString("arrival_time").toString().chunked(2)[1]

            val hDeparture = stopObj.getString("departure_time").toString().chunked(2)[0]
            val mDeparture = stopObj.getString("departure_time").toString().chunked(2)[1]

            val stopPoint = stopObj.getJSONObject("stop_point")

            val codeUIc = stopPoint.getString("id").toString().split(':', ignoreCase = false, limit = 4)[2]

            val lat = stopPoint.getJSONObject("coord").getDouble("lat")
            val lon = stopPoint.getJSONObject("coord").getDouble("lon")

            val lib = stopPoint.getString("name")

            val station = Station(
                codeUIc.toInt(),
                lib,
                lon,
                lat
            )

            val stop = Stops(hArrival, mArrival, hDeparture, mDeparture)
            stop.setStation(station)

            train.addStops(stop, !stopObj.getBoolean("drop_off_allowed"), !stopObj.getBoolean("pickup_allowed") || i == stops.length() - 1)
        }

        return train
    }
}