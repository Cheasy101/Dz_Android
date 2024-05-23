package com.example.dzandroid1


import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dzandroid1.API.RetrofitInstance
import com.example.dzandroid1.Util.Constants.Companion.IMG_URL
import com.example.dzandroid1.Util.Constants.Companion.PERMISSION_REQUEST_CODE
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    var username: String by mutableStateOf("")
    var password: String by mutableStateOf("")
    var lat: Double by mutableDoubleStateOf(0.0)
    var lon: Double by mutableDoubleStateOf(0.0)
    var temperature: Int by mutableIntStateOf(0)
    var feelsLike: Int by mutableIntStateOf(0)
    var pressure: Double by mutableDoubleStateOf(0.0)
    var description: String by mutableStateOf("")
    var iconUrl: String by mutableStateOf("")


    val userDao: UserDao by lazy { (application as MyApplication).userDao }

    fun getWeatherByCurrentPosition(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val lat = location.latitude
                    val lon = location.longitude
                    viewModelScope.launch {
                        getWeatherByCoordinates(lat, lon)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("WeatherScreen", "Error getting location: ${e.message}")
            }
    }

    private suspend fun getWeatherByCoordinates(lat: Double, lon: Double) {
        val response = RetrofitInstance.api.getWeatherByCoordinates(lat,lon)
        if (response.isSuccessful) {
            response.body()?.let { weatherResponse ->
                this.lat = weatherResponse.coord.lat
                this.lon = weatherResponse.coord.lon
                temperature = kelvinToCelsius(weatherResponse.main.temp)
                feelsLike = kelvinToCelsius(weatherResponse.main.feelsLike)
                pressure = hPaToMm(weatherResponse.main.pressure)
                description = weatherResponse.weather[0].description
                iconUrl = iconIdToUrl(weatherResponse.weather[0].icon)
            }
        }
    }

    fun getWeatherByEnteredCity(city: String) {
        viewModelScope.launch {
            val response = RetrofitInstance.api.getWeatherByCity(city)
            if (response.isSuccessful) {
                response.body()?.let { weatherResponse ->
                    lat = weatherResponse.coord.lat
                    lon = weatherResponse.coord.lon
                    temperature = kelvinToCelsius(weatherResponse.main.temp)
                    feelsLike = kelvinToCelsius(weatherResponse.main.feelsLike)
                    pressure = hPaToMm(weatherResponse.main.pressure)
                    description = weatherResponse.weather[0].description
                    iconUrl = iconIdToUrl(weatherResponse.weather[0].icon)
                }
            }
        }
    }

    fun kelvinToCelsius(kelvin: Double): Int{
        val celsius = kelvin - 273
        return celsius.toInt()
    }

    fun hPaToMm(hPa: Double): Double {
        return  hPa*0.75
    }

    fun iconIdToUrl(iconId: String): String {
        return "$IMG_URL$iconId.png"
    }

    suspend fun hasCurrentUser(): Boolean {
        val currentUser = userDao.getCurrentUser().firstOrNull()
        return currentUser != null
    }


}
