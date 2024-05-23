package com.example.dzandroid1.Models

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val coord: Coord,
    val weather: List<Weather>,
    val main: Main,
    val name: String
)

data class Coord (
    val lon: Double,
    val lat: Double
)
data class Weather (
    val description: String,
    val icon: String
)

data class Main(
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    val pressure: Double
)