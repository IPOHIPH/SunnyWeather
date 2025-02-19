package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class DailyResponse(val status : String , val result : Result){

    data class Result(val daily : Daily)

    data class Daily(
        val temperature: List<Temperature>,
        val skycon: List<Skycon>,
        val ultraviolet: List<LifeDescription>,  // 直接使用
        val carWashing: List<LifeDescription>,
        val dressing: List<LifeDescription>,
        val comfort: List<LifeDescription>,
        val coldRisk: List<LifeDescription>
    )

    data class Temperature(val max : Float , val min : Float)

    data class Skycon(val value : String , val date : Date)


    data class LifeDescription(val desc : String)
}