package com.example.dzandroid1.Widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.dzandroid1.API.RetrofitInstance
import com.example.dzandroid1.MainActivity
import com.example.dzandroid1.MyApplication
import com.example.dzandroid1.User
import com.example.dzandroid1.UserDao
import com.example.dzandroid1.Util.Constants
import com.google.android.gms.location.LocationServices
import com.volsib.logincompose.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class WeatherWidget : GlanceAppWidget() {
    private var temperature by mutableStateOf("")
    private var feelsLike by mutableStateOf("")
    private var city by mutableStateOf("")
    private var updateAt by mutableStateOf("")

    private var iconBitmap by mutableStateOf<Bitmap?>(null)

    private var isNotSignedIn by mutableStateOf(false)

    private var noRights by mutableStateOf(false)


    @SuppressLint("MissingPermission", "CoroutineCreationDuringComposition")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "weatherWidgetWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequest.Builder(
                WeatherWidgetWorker::class.java,
                60.minutes.toJavaDuration()
            ).build()
        )

        provideContent {
            val userDao: UserDao by lazy { (context.applicationContext as MyApplication).userDao }
            val currentUser by userDao.getCurrentUser()
                .collectAsState(initial = User(0, "", "", false))

            val coroutineScope = rememberCoroutineScope()
            val callback: () -> Unit = {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            val lat = location.latitude
                            val lon = location.longitude
                            coroutineScope.launch {
                                getWeatherByCoordinates(lat, lon)
                                iconBitmap = loadImageAsBitmap(iconUrl)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("WeatherWidget", "Error getting location: ${e.message}")
                    }
            }

            isNotSignedIn = currentUser == null

            noRights = ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED

            GlanceTheme {

                if (isNotSignedIn) {

                    DefaultContent()
                } else {
                    if (noRights) {
                        ErrorScreen()
                    } else{
                        WeatherContent(callback)
                        coroutineScope.launch {
                            WeatherWidget().updateAll(context.applicationContext)
                        }
                    }
                }
            }
        }
    }

    var description: String by mutableStateOf("")
    var iconUrl: String by mutableStateOf("")

    fun kelvinToCelsius(kelvin: Double): String {
        val celsius = kelvin - 273
        return celsius.toInt().toString()
    }

    fun iconIdToUrl(iconId: String): String {
        return "${Constants.IMG_URL}$iconId.png"
    }

    private suspend fun getWeatherByCoordinates(lat: Double, lon: Double) {
        val response = RetrofitInstance.api.getWeatherByCoordinates(lat, lon)
        if (response.isSuccessful) {
            response.body()?.let { weatherResponse ->
                temperature = kelvinToCelsius(weatherResponse.main.temp)
                feelsLike = kelvinToCelsius(weatherResponse.main.feelsLike)
                description = weatherResponse.weather[0].description
                iconUrl = iconIdToUrl(weatherResponse.weather[0].icon)
                city = weatherResponse.name

                val dateFormat = SimpleDateFormat("HH:mm")
                val currentTime = Date()
                updateAt = dateFormat.format(currentTime)
            }
        }
    }

    private suspend fun loadImageAsBitmap(url: String): Bitmap? {
        return try {
            withContext(Dispatchers.IO) {
                BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Composable
    private fun ErrorScreen() {
        Column(
            modifier = GlanceModifier.fillMaxSize()
                .clickable(onClick = actionStartActivity<MainActivity>())
                .background(GlanceTheme.colors.background)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Дай права")
        }
    }

    @Composable
    private fun DefaultContent() {
        Column(
            modifier = GlanceModifier.fillMaxSize()
                .clickable(onClick = actionStartActivity<MainActivity>())
                .background(GlanceTheme.colors.background)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Надо авторизоваться")
        }
    }

    @Composable
    private fun WeatherContent(callback: () -> Unit) {
        LaunchedEffect(key1 = Unit) {
            callback()
        }
        Column(
            modifier = GlanceModifier.fillMaxSize()
                .clickable(onClick = actionStartActivity<MainActivity>())
                .background(GlanceTheme.colors.background)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconBitmap == null) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(text = "Обновлено в ${updateAt}")
                    Image(
                        modifier = GlanceModifier.size(40.dp).clickable(callback),
                        provider = ImageProvider(R.drawable.ic_refresh),
                        contentDescription = "Refresh icon"
                    )
                }
                Text(text = city, modifier = GlanceModifier.padding(bottom = 12.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "${temperature}°C",
                        style = TextStyle(fontSize = 20.sp)
                    )
                    Spacer(modifier = GlanceModifier.width(5.dp))
                    Image(
                        modifier = GlanceModifier.size(40.dp),
                        provider = if (iconBitmap != null) {
                            ImageProvider(iconBitmap!!)
                        } else {
                            ImageProvider(R.drawable.ic_launcher_foreground)
                        },
                        contentDescription = "Weather icon"
                    )
                    Column(
                        modifier = GlanceModifier.padding(horizontal = 5.dp)
                    ) {
                        Text(text = description)
                        Text(text = "Ощущается как ${feelsLike}°C")
                    }
                }
            }
        }
    }
}