package com.example.dzandroid1.API

import com.example.dzandroid1.Models.WeatherResponse
import com.example.dzandroid1.Util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("/data/2.5/weather")
    suspend fun getWeatherByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = API_KEY,
    ): Response<WeatherResponse>

    @GET("/data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String = API_KEY
    ): Response<WeatherResponse>
}