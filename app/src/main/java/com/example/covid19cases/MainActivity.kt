package com.example.covid19cases

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SimpleAdapter
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.covid19cases.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import com.robinhood.ticker.TickerUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private const val BASE_URL = "https://api.covidtracking.com/v1/"
private const val TAG = "MainActivity"
private const val ALL_STATES = "All (National)"

class MainActivity : AppCompatActivity() {

    private lateinit var currentlyShownData: List<CovidData>
    private lateinit var adapter: CovidSparkAdapter
    private lateinit var perStateDailyData: Map<String, List<CovidData>>
    private lateinit var nationalDailyData: List<CovidData>
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val covidService = retrofit.create(CovidService::class.java)

        //Fetch the national data
        covidService.getNationalData().enqueue(object : Callback<List<CovidData>> {
            override fun onResponse(
                call: Call<List<CovidData>>,
                response: Response<List<CovidData>>
            ) {
                Log.i(TAG, "OnResponse $response")
                val nationalData = response.body()
                if(nationalData == null){
                    Log.w(TAG, "NOT A VALID RESPONSE WAS RECEIVED IN BODY!")
                    return
                }
                setupEventListeners()
                nationalDailyData = nationalData.reversed()
                Log.i(TAG,"Update graph with national data")
                updateDisplayWithData(nationalDailyData)
            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG, "OnFailure $t")
            }

        })

        //Fetch the state data
        covidService.getStatesData().enqueue(object : Callback<List<CovidData>> {
            override fun onResponse(
                call: Call<List<CovidData>>,
                response: Response<List<CovidData>>
            ) {
                Log.i(TAG, "OnResponse $response")
                val statesData = response.body()
                if(statesData == null){
                    Log.w(TAG, "NOT A VALID RESPONSE WAS RECEIVED IN BODY!")
                    return
                }
                perStateDailyData = statesData.reversed().groupBy { it.state }
                Log.i(TAG,"Update spinner with state names")

                // Update spinner with state names
                updateSpinnerWithStateData(perStateDailyData.keys)
            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG, "OnFailure $t")
            }

        })
    }

    private fun updateSpinnerWithStateData(stateNames: Set<String>) {
        val stateAbbreviationList = stateNames.toMutableList()
        stateAbbreviationList.sort()
        stateAbbreviationList.add(0, ALL_STATES)

        // Add state list as data source for the spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, stateAbbreviationList)
        binding.spinnerSelect.adapter = adapter
        binding.spinnerSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedState = parent?.getItemAtPosition(position) as String
                val selectedData = perStateDailyData[selectedState] ?: nationalDailyData
                updateDisplayWithData(selectedData)
            }

        }
    }

    private fun setupEventListeners() {

        binding.tickerView.setCharacterLists(TickerUtils.provideNumberList())

        // Add a listener for the user scrubbing on the chart
        binding.sparkView.isScrubEnabled = true
        binding.sparkView.setScrubListener { itemData ->
            if(itemData is CovidData){
                updateInfoForDate(itemData)
            }
        }

        // Respond to radio button selected events
        binding.rgGroupTimeSelection.setOnCheckedChangeListener{ _, checkedId ->
            adapter.daysAgo = when (checkedId){
                binding.rbWeek.id -> TimeScale.WEEK
                binding.rbMonth.id -> TimeScale.MONTH
                binding.rbMax.id -> TimeScale.MAX
                else -> TimeScale.MAX
            }
            adapter.notifyDataSetChanged()
        }

        binding.rgGroupMetricSelection.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId){
                binding.rbPositive.id -> updateDisplayMetric(Metric.POSITIVE)
                binding.rbNegative.id -> updateDisplayMetric(Metric.NEGATIVE)
                binding.rbDeath.id -> updateDisplayMetric(Metric.DEATH)
            }

        }
    }

    private fun updateDisplayMetric(metric: Metric) {


        // Update the color of the chart
        val colorRes = when(metric){
            Metric.NEGATIVE -> R.color.negative
            Metric.POSITIVE -> R.color.positive
            Metric.DEATH -> R.color.death
        }
        @ColorInt val colorInt = ContextCompat.getColor(this, colorRes)
        binding.sparkView.lineColor = colorInt
        binding.tickerView.setTextColor(colorInt)

        // Update the metric on the adapter
        adapter.metric = metric
        adapter.notifyDataSetChanged()
        
        // Reset number and date shown in the text views
        updateInfoForDate(currentlyShownData.last())
    }

    private fun updateDisplayWithData(dailyData: List<CovidData>) {
        currentlyShownData = dailyData
                
        // Create a new SparkAdapter with the data
        adapter = CovidSparkAdapter(dailyData)
        binding.sparkView.adapter = adapter

        // Update radio buttons to select the positive cases and max time by default
        binding.rbPositive.isChecked = true
        binding.rbMax.isChecked = true

        // Display metric for the most recent date
        updateInfoForDate(dailyData.last())
    }

    private fun updateInfoForDate(covidData: CovidData) {
        val numCases = when (adapter.metric){
            Metric.NEGATIVE -> covidData.negativeIncrease
            Metric.POSITIVE -> covidData.positiveIncrease
            Metric.DEATH -> covidData.deathIncrease
        }
        binding.tickerView.text = NumberFormat.getInstance().format(numCases)
        val outputDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        binding.tvDateLabel.text = outputDateFormat.format(covidData.dateChecked)
    }
}