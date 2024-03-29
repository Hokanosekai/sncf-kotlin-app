package fr.hoka.sncf

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ListView
import fr.hoka.sncf.entities.Station
import fr.hoka.sncf.entities.Train
import fr.hoka.sncf.services.ApiSNCF
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import java.sql.DriverManager.println

class MainActivity : AppCompatActivity() {
    private val api: ApiSNCF = ApiSNCF(
        "https://api.navitia.io/v1",
        "d20f804d-aa65-4270-94c9-429e2b36fc2b"
    ) // Instantiate SNCF API service
    private lateinit var selectedStation: Station // Selected station by the user (default null)
    private var trains: List<Train> =
        arrayListOf() // List of all departures Trains from the selected station (default empty)
    private var trainsViewAdapter: ArrayAdapter<Train>? = null // Trains List View adapter
    private lateinit var trainsListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize all stations from the CSV file `res/raw/gares.csv`
        val stations = initListStations()

        // Search station input
        val input = findViewById<AutoCompleteTextView>(R.id.search_input)

        // List view
        trainsListView = findViewById(R.id.trains_list)
        trainsViewAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, trains)
        trainsListView.adapter = trainsViewAdapter

        // Our adapter to propose the station by matching her name
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1, stations
        )
        input.setAdapter(adapter)

        // On station selected we fetch all trains
        input.setOnItemClickListener { parent, _, position, _ ->
            this.selectedStation = parent.getItemAtPosition(position) as Station
            trainsViewAdapter?.clear()
            api.fetchDeparturesFromStopArea(selectedStation.code_uic, cb = object : Callback {
                // On failure do nothing (In normal case we will handle the error)
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                // On Success parse the response body
                override fun onResponse(call: Call, response: Response) {
                    this@MainActivity.runOnUiThread {
                        val trains =
                            api.parseJSONDepartures(response.body as ResponseBody, selectedStation)

                        // Fetch all stops for each train
                        try {
                            for (train in trains) {
                                api.fetchVehicleJourney(
                                    train.vehicle_journey_id,
                                    cb = object : Callback {
                                        override fun onFailure(call: Call, e: IOException) {
                                            e.printStackTrace()
                                        }

                                        override fun onResponse(call: Call, response: Response) {
                                            // Parse response body into Stops entities
                                            api.parseJSONVehicleJourneys(train, response.body!!)
                                        }
                                    })
                            }
                        } catch (e: Exception) {
                            println(e)
                        } finally {
                            println("Finally")

                            // Parse response body into Trains entities
                            trainsViewAdapter!!.addAll(trains)
                            // Update the listView
                            trainsViewAdapter?.notifyDataSetChanged()
                            trainsListView.adapter = trainsViewAdapter
                            // Hide keyboard
                            hideKeyboard()
                        }
                    }
                }
            })
        }

        // On train selected we open the map activity
        trainsListView.setOnItemClickListener { parent, _, position, _ ->
            // Get the selected train
            val el = parent.getItemAtPosition(position) as Train
            // Create intent to open the map activity
            val intent = Intent(this, MapsActivity::class.java)
            // Pass the train to the map activity
            intent.putExtra("train", el)
            // Start the map activity
            startActivity(intent)
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

    /**
     * Hide keyboard
     */
    private fun hideKeyboard() {
        this@MainActivity.currentFocus?.let { view ->
            val inputMethodManager =
                getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}