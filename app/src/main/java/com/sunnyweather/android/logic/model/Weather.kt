package com.sunnyweather.android.logic.model

data class Weather (val realtime : RealtimeResponse.Result , val daily : DailyResponse.Daily){
}