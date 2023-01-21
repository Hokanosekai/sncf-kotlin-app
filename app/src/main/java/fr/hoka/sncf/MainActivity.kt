package fr.hoka.sncf

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ListView
import fr.hoka.sncf.entities.Station
import fr.hoka.sncf.entities.Train
import fr.hoka.sncf.services.ApiSNCF
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val api: ApiSNCF = ApiSNCF("https://api.navitia.io/v1", "d20f804d-aa65-4270-94c9-429e2b36fc2b") // Instantiate SNCF API service
    lateinit var selectedStation: Station // Selected station by the user (default null)
    private var trains: List<Train> = ArrayList<Train>() // List of all departures Trains from the selected station (default empty)
    lateinit var trainsViewAdapter: ArrayAdapter<Train> // Trains List View adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize all stations from the CSV file `res/raw/gares.csv`
        val stations = initListStations()

        // Search station input
        val input = findViewById<AutoCompleteTextView>(R.id.search_input)

        // List view
        val trainsListView = findViewById<ListView>(R.id.trains_list)
        trainsViewAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, trains)

        // Our adapter to propose the station by matching her name
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, stations)
        input.setAdapter(adapter)

        // On station selected we fetch all trains
        input.setOnItemClickListener {parent, view, position, id ->
            this.selectedStation = parent.getItemAtPosition(position) as Station
            api.fetchDeparturesFromStopArea(selectedStation.code_uic, cb = object: Callback{
                // On failure do nothing (In normal case we will handle the error)
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                // On Success parse the response body
                override fun onResponse(call: Call, response: Response) {
                    // Parse response body into Trains entities
                    trains = api.parseJSON(response.body)
                    // Update Adapter
                    trainsViewAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, trains)

                    // Update the listView
                    this@MainActivity.runOnUiThread(java.lang.Runnable {
                        trainsViewAdapter.notifyDataSetChanged()
                        trainsListView.adapter = trainsViewAdapter
                    })
                }
            })
        }
    }

    /**
     * Read CSV file that contains stations.
     * @return List of Station.
     */
    private fun initListStations(): List<Station> {
        // Open file, and read it
        val inputStream = resources.openRawResource(R.raw.gares)
        val reader = inputStream.bufferedReader()

        // Read the first line to remove the columns CSV definition
        val header = reader.readLine()

        // Read the rest of the lines, then map through it to return a Station entity
        return reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                // Split the line to get data
                val (code, lib, lg, lat) = it.split(';', ignoreCase = false, limit = 4)
                // Instantiate a Station entity with data
                Station(code.toInt(), lib, lg.toDouble(), lat.toDouble())
            }.toList()
    }
}