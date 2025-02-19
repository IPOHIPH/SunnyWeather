package com.sunnyweather.android.logic

import android.util.Log
import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetWork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

//仓库层，供ViewModel使用
object Repository {


    fun savePlace(place:Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()

    fun refreshWeather(lng : String , lat : String) = fire(Dispatchers.IO){
        coroutineScope{
            val deferredRealtime = async{SunnyWeatherNetWork.getRealtimeWeather(lng , lat)}
            val deferredDaily =  async{SunnyWeatherNetWork.getDailyWeather(lng , lat)}

            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()

            if(realtimeResponse.status == "ok" && dailyResponse.status == "ok"){
                val weather = Weather(realtimeResponse.result , dailyResponse.result.daily)
                Result.success(weather)

            }else {
                Result.failure(RuntimeException(
                        "realtime status is ${realtimeResponse.status} , " +
                        "daily response status is  ${dailyResponse.status}"))
            }
        }
    }



    fun searchPlaces(query : String) = fire(Dispatchers.IO){
        val placeResponse = SunnyWeatherNetWork.searchPlaces(query)
        if(placeResponse.status == "ok"){
            val places = placeResponse.places
            Result.success(places)
        }else{
            Result.failure(RuntimeException("response status id ${placeResponse.status}"))
        }
    }

    private fun <T> fire (context : CoroutineContext, block : suspend () -> Result<T>) =
        liveData<Result<T>>(context){
        val result = try{
            block()
        }catch(e : Exception){
            Result.failure<T>(e)
        }
        emit(result)
    }



}