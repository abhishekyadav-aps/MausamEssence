package com.abhitom.mausamessence.fragments

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.abhitom.mausamessence.DashBoard
import com.abhitom.mausamessence.R
import com.abhitom.mausamessence.databinding.FragmentDateBinding
import com.abhitom.mausamessence.retrofit.OneCallResponse
import com.abhitom.mausamessence.retrofit.RetroFitClient
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DateFragment : Fragment(),DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {

    private var monthSaver: Int=0
    private var yearSaver: Int=0
    private var dayOfMonthSaver: Int=0
    private var binding: FragmentDateBinding? = null
    private lateinit var datePickerDialog: DatePickerDialog
    private var cities:MutableList<String> = mutableListOf()
    private var currentCity = 0
    private val mumbaiLoc= LatLng(19.01441,72.847939)
    private val noidaLoc= LatLng(28.496149,77.536011)
    private val delhiLoc= LatLng(28.666668,77.216667)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDateBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        changeBackground()

        cities.clear()
        cities.add("Select City")
        cities.add("Delhi")
        cities.add("Mumbai")
        cities.add("Noida")
        HideThem()

        val userNameText="Hi, "+DashBoard.userName+"!"
        binding?.txtUsername1!!.text=userNameText

        val cityArrayAdapter = ArrayAdapter(context!!, R.layout.spinner_text_item, cities)
        binding?.spinCity?.adapter = cityArrayAdapter
        binding?.spinCity?.onItemSelectedListener = this
        binding?.spinCity?.setSelection(0, true)

        val currentDate: Long = java.lang.Long.valueOf(System.currentTimeMillis())
        val currentDatedf = Date(currentDate)
        val currentDatevv = SimpleDateFormat("dd-MM-yyyy").format(currentDatedf)
        binding?.tvDFDate?.text=currentDatevv

        val currentdd = SimpleDateFormat("dd").format(currentDatedf)
        val currentmm = SimpleDateFormat("MM").format(currentDatedf)
        val currentyy= SimpleDateFormat("yyyy").format(currentDatedf)
        dayOfMonthSaver=currentdd.toInt()
        monthSaver=currentmm.toInt()-1
        yearSaver=currentyy.toInt()

        btnSearchOnClickListener()

        tvDFDateOnClickListener()
    }

    private fun tvDFDateOnClickListener() {
        val currentDate: Long = java.lang.Long.valueOf(System.currentTimeMillis())
        val currentDatedf = Date(currentDate)
        val currentDatevv = SimpleDateFormat("dd-MM-yyyy").format(currentDatedf)
        binding?.tvDFDate?.text=currentDatevv

        binding?.tvDFDate?.setOnClickListener {
            datePickerDialog=
                DatePickerDialog(activity!!, this, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(
                    Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
            datePickerDialog.datePicker.minDate= Calendar.getInstance().timeInMillis
            val c= Calendar.getInstance()
            c.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + 7)
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
        }
    }

    private fun getData(loc: LatLng) {
        RetroFitClient.instance.service.oneCallApi(loc.latitude, loc.longitude, DashBoard.apiKey, DashBoard.units)
            .enqueue(object : Callback<OneCallResponse> {
                override fun onResponse(
                    call: Call<OneCallResponse>,
                    response: Response<OneCallResponse>
                ) {
                    if (response.isSuccessful) {
                        showData(response)
                    } else {
                        toastMaker(response.errorBody().toString(), DashBoard.dateFragment)
                    }
                }

                override fun onFailure(call: Call<OneCallResponse>, t: Throwable) {
                    toastMaker("No Internet / Server Down", DashBoard.dateFragment)
                }
            })

    }

    private fun toastMaker(message: String?, Fragment: Fragment) {
        if (Fragment.isVisible) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun btnSearchOnClickListener() {
        binding?.btnSearch?.setOnClickListener {
            when(currentCity){
                0 -> {
                    HideThem()
                }
                1 ->{
                    getData(delhiLoc)
                    ShowThem()
                }
                2->{
                    getData(mumbaiLoc)
                    ShowThem()
                }
                3->{
                    getData(noidaLoc)
                    ShowThem()
                }
            }
        }
    }

    private fun showData(response: Response<OneCallResponse>) {
        for (i in response.body()?.daily?.indices!!){
            val sunrise: Long = response.body()?.daily!![i]?.dt.let { java.lang.Long.valueOf(it!!) }!! * 1000
            val sunrisedf = Date(sunrise)
            val sunrisedd = SimpleDateFormat("dd").format(sunrisedf)
            val sunriseMM = SimpleDateFormat("MM").format(sunrisedf)
            val sunriseyy = SimpleDateFormat("yyyy").format(sunrisedf)
            if (sunrisedd.toInt()==dayOfMonthSaver && sunriseMM.toInt()==(monthSaver+1) && sunriseyy.toInt()==yearSaver){
                Log.i("TAGGERRR", "$sunrisedd ${sunriseMM.toInt()} $sunriseyy - $dayOfMonthSaver ${monthSaver+1} $yearSaver")
                binding?.txtTemp?.text= response.body()?.daily!![i]?.temp?.day.toString()
                binding?.txtWeather?.text= response.body()?.daily!![i]?.weather?.get(0)?.main
                val sunris: Long = response.body()?.daily!![i]?.sunrise?.let { java.lang.Long.valueOf(it) }!! * 1000
                val sunrisdf = Date(sunris)
                val sunrisvv = SimpleDateFormat("hh:mm a").format(sunrisdf)
                binding?.txtSunrise?.text=sunrisvv
                val sunset: Long = response.body()?.daily!![i]?.sunset?.let { java.lang.Long.valueOf(it) }!! * 1000
                val sunsetdf = Date(sunset)
                val sunsetvv = SimpleDateFormat("hh:mm a").format(sunsetdf)
                binding?.txtSunset?.text=sunsetvv

                val humidity=response.body()?.daily!![i]?.humidity.toString()+"%"
                val pressure=response.body()?.daily!![i]?.pressure.toString()+" hPa"
                val visibility=response.body()?.daily!![i]?.clouds.toString()
                binding?.txtHumidity?.text= humidity
                binding?.txtPressure?.text= pressure
                binding?.txtVisiblity?.text= visibility
                binding?.txtUv?.text= response.body()?.daily!![i]?.uvi.toString()

                if (DashBoard.units=="metric"){
                    val feelsLike=response.body()?.daily!![i]?.feelsLike?.day.toString()+" °C"
                    val windSpeed=response.body()?.daily!![i]?.windSpeed.toString()+" m/s"
                    val minTemp= response.body()!!.daily?.get(i)?.temp?.min.toString() + " °C"
                    val maxTemp= response.body()!!.daily?.get(i)?.temp?.max.toString() + " °C"
                    binding?.txtFeelsLike?.text= feelsLike
                    binding?.txtWindSpeed?.text= windSpeed
                    binding?.txtDateMin?.text=minTemp
                    binding?.txtDateMax?.text=maxTemp
                    binding?.txtDegree?.text="°C"
                }else{
                    val feelsLike=response.body()?.daily!![i]?.feelsLike?.day.toString()+" °F"
                    val windSpeed=response.body()?.daily!![i]?.windSpeed.toString()+" miles/s"
                    val minTemp= response.body()!!.daily?.get(i)?.temp?.min.toString() + " °F"
                    val maxTemp= response.body()!!.daily?.get(i)?.temp?.max.toString() + " °F"
                    binding?.txtFeelsLike?.text= feelsLike
                    binding?.txtWindSpeed?.text= windSpeed
                    binding?.txtDateMin?.text=minTemp
                    binding?.txtDateMax?.text=maxTemp
                    binding?.txtDegree?.text="°F"
                }
            }
        }
    }

    private fun ShowThem(){
        binding?.cardView?.visibility = View.VISIBLE
        binding?.txtWeather?.visibility = View.VISIBLE
        binding?.txtDegree?.visibility = View.VISIBLE
        binding?.txtTemp?.visibility = View.VISIBLE
        binding?.txtDateMax?.visibility = View.VISIBLE
        binding?.txtDateMin?.visibility = View.VISIBLE
    }

    private fun HideThem(){
        binding?.cardView?.visibility = View.INVISIBLE
        binding?.txtWeather?.visibility = View.INVISIBLE
        binding?.txtDegree?.visibility = View.INVISIBLE
        binding?.txtTemp?.visibility = View.INVISIBLE
        binding?.txtDateMax?.visibility = View.INVISIBLE
        binding?.txtDateMin?.visibility = View.INVISIBLE
    }

    private fun changeBackground() {
        val currentTime: Long = System.currentTimeMillis()
        val currentTimeDate = Date(currentTime)
        val currentTimeFormat = SimpleDateFormat("HH").format(currentTimeDate)

        when {
            (currentTimeFormat.toInt()>6) and (currentTimeFormat.toInt()<12) -> {
                if (DashBoard.dateFragment.isVisible) {
                    binding?.clDFLayout?.background = context?.let { ContextCompat.getDrawable(it, R.drawable.sunrisehd) }
                }
            }
            (currentTimeFormat.toInt()>=16) and (currentTimeFormat.toInt()<21) -> {
                if (DashBoard.dateFragment.isVisible) {
                    binding?.clDFLayout?.background = context?.let { ContextCompat.getDrawable(it, R.drawable.sunsethd) }
                }
            }
            (currentTimeFormat.toInt()>=12) and (currentTimeFormat.toInt()<16) -> {
                if (DashBoard.dateFragment.isVisible) {
                    binding?.clDFLayout?.background = context?.let { ContextCompat.getDrawable(it, R.drawable.noonhd) }
                }
            }
            else -> {
                if (DashBoard.dateFragment.isVisible) {
                    binding?.clDFLayout?.background = context?.let { ContextCompat.getDrawable(it, R.drawable.nighthd) }
                }
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val date=dayOfMonth.toString()+"-"+(month+1).toString()+"-"+year.toString()
        binding?.tvDFDate?.text=date
        dayOfMonthSaver=dayOfMonth
        yearSaver=year
        monthSaver=month
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if(parent?.id == R.id.spin_city){
            currentCity = id.toInt()
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

}