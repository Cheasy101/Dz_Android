package com.example.dzandroid1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.dzandroid1.Widget.WeatherWidget
import com.volsib.logincompose.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


class SecondActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuccessScreen(this)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SuccessScreen(activity: ComponentActivity) {
    val viewModel: LoginViewModel = viewModel()
    var city by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val userDao = viewModel.userDao

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black), // Здесь вы можете настроить прозрачность черного цвета
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Добавляем отступы со всех сторон
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text(stringResource(R.string.enter_city), color = Color.White) }, // Устанавливаем белый цвет текста
                singleLine = true,
                modifier = Modifier.padding(bottom = 16.dp), // Добавляем отступ снизу
                textStyle = TextStyle(color = Color.White) // Устанавливаем белый цвет текста в поле ввода
            )
            Button(
                onClick = {
                    if (city.isNotBlank()) {
                        viewModel.getWeatherByEnteredCity(city)
                    }
                },
                modifier = Modifier.padding(bottom = 16.dp) // Добавляем отступ снизу
            ) {
                Text(
                    text = stringResource(R.string.update_weather_by_city),
                    color = Color.White // Устанавливаем белый цвет текста
                )
            }
            Button(
                onClick = {
                    viewModel.getWeatherByCurrentPosition(context)
                },
                modifier = Modifier.padding(bottom = 16.dp) // Добавляем отступ снизу
            ) {
                Text(
                    text = stringResource(R.string.update_weather_by_current_place),
                    color = Color.White // Устанавливаем белый цвет текста
                )
            }

            Text(
                text = "Температура: " + viewModel.temperature.toString() + "°C",
                color = Color.White // Устанавливаем белый цвет текста
            )
            Text(
                text = "Ощущается как: " + viewModel.feelsLike.toString() + "°C",
                color = Color.White // Устанавливаем белый цвет текста
            )
            Text(
                text = "Давление: " + viewModel.pressure.toString() + " мм р.с.",
                color = Color.White // Устанавливаем белый цвет текста
            )

            Text(
                text = "Координаты: " + viewModel.lon.toString() + ", " + viewModel.lat.toString(),
                color = Color.White // Устанавливаем белый цвет текста
            )

            GlideImage(
                modifier = Modifier.size(200.dp),
                model = viewModel.iconUrl,
                contentDescription = stringResource(R.string.weather_icon),
                contentScale = ContentScale.Fit
            )

            Text(
                text = viewModel.description,
                color = Color.White // Устанавливаем белый цвет текста
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        val currentUser = userDao.getCurrentUser().firstOrNull()!!
                        currentUser.isSigned = false
                        userDao.update(currentUser)

                        // Updating the app widget
                        WeatherWidget().updateAll(context)
                        val intent = Intent(activity, MainActivity::class.java)
                        activity.startActivity(intent)
                    }
                },
            ) {
                Text(text = stringResource(R.string.exit))
            }

        }
    }
}