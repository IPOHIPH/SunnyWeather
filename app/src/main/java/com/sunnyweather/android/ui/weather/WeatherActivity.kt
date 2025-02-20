package com.sunnyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityMainBinding
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.databinding.ForecastBinding
import com.sunnyweather.android.databinding.ForecastItemBinding
import com.sunnyweather.android.databinding.LifeIndexBinding
import com.sunnyweather.android.databinding.NowBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky

class WeatherActivity : AppCompatActivity() {
    private lateinit  var _binding : ActivityWeatherBinding

    val binding : ActivityWeatherBinding
        get() = _binding

    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java)}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        _binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        if(viewModel.locationLng.isEmpty()){
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if(viewModel.locationLat.isEmpty()){
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if(viewModel.placeName.isEmpty()){
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        viewModel.weatherLiveData.observe(this , Observer{result->
            val weather = result.getOrNull()
            if(weather != null){
                showWeatherInfo(weather)
            }else{
                Toast.makeText(this,"无法成功获取天气信息",Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.swipeRefresh.isRefreshing = false
        })
        binding.swipeRefresh.setColorSchemeResources((com.google.android.material.R.color.design_default_color_primary))
        refreshWeather()
        binding.swipeRefresh.setOnRefreshListener { refreshWeather() }


        binding.now.navBtn.setOnClickListener{
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }


        })


    }

    fun refreshWeather(){
        viewModel.refreshWeather(viewModel.locationLng , viewModel.locationLat)
        binding.swipeRefresh.isRefreshing = true
    }

    private fun showWeatherInfo(weather : Weather){
        val realtime = weather.realtime
        val daily = weather.daily

        //填充now.xml 中的数据
        val nowBinding = binding.now
        binding.now.placeName.text = viewModel.placeName
        val currentTempText =  "${realtime.temperature.toInt()} ℃"
        nowBinding.currentTemp.text = currentTempText
        nowBinding.currentSky.text = getSky(realtime.skycon).info
        val currentPm25Text = "空气指数 ${realtime.aqi.toInt()}"
        nowBinding.currentAQI.text = currentPm25Text
        nowBinding.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        //填充forecast.xml 中的数据
        val forecastBinding =binding.forecast
        forecastBinding.forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for(i in 0 until days){
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastBinding.forecastLayout , false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.getDefault())

            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            forecastBinding.forecastLayout.addView(view)
        }
        //填充life_index.xml布局中的数据
        val lifeIndexBinding =  binding.lifeIndex
        lifeIndexBinding.coldRiskText.text = daily.coldRisk[0].desc
        lifeIndexBinding.ultravioletText.text = daily.ultraviolet[0].desc
        lifeIndexBinding.carWashingText.text = daily.carWashing[0].desc
        lifeIndexBinding.dressingText.text = daily.dressing[0].desc
        binding.weatherLayout.visibility = View.VISIBLE
    }

}