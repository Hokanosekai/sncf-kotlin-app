package fr.hoka.sncf.services

import fr.hoka.sncf.entities.Train
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

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
     * @param cb Callback object
     * @return The 8 first Train.
     */
    fun fetchDeparturesFromStopArea(codeUIC: Int, cb: Callback) {
        // Build the uri
        val uri = "$baseUri/coverage/sncf/stop_areas/stop_area:SNCF:$codeUIC/departures?key=$token"

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
    fun parseJSON(json: ResponseBody): List<Train> {
        val tmpTrains: ArrayList<Train> = ArrayList()
        try {
            val obj = JSONObject(json.string())
            // Parse as JSONArray departures
            val departures: JSONArray = obj.getJSONArray("departures")

            // Iterate through departures (limit to 8)
            for (i in 0 until 8) {
                // train object
                val trainObj = departures.getJSONObject(i)

                // display_informations and stop_date_time
                val trainInfo = trainObj.getJSONObject("display_informations")
                val trainStopDateTime = trainObj.getJSONObject("stop_date_time")

                // Train Code, Train Destination, Train Source
                val num: Int = trainInfo.getInt("headsign")
                val dest: String = trainInfo.getString("direction")
                val prov: String = trainInfo.getString("direction")

                // The train departure date time
                val timestamp = trainStopDateTime.getString("departure_date_time")
                // split it to get only hours and minutes then split it again because we don't want seconds
                val h: Int = timestamp.split("T").last().chunked(2)[0].toInt()
                val m: Int = timestamp.split("T").last().chunked(2)[1].toInt()

                // Add the new train to the tmp list
                tmpTrains.add(Train(num, dest, prov, h, m))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        println("parseJSON >> ${tmpTrains.size}")
        return tmpTrains.toList()
    }
}