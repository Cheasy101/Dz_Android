package com.example.dzandroid1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.volsib.logincompose.R
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegistrationAndLogin(this)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RegistrationAndLogin(activity: ComponentActivity) {
    val pagesCount = 2
    val pagerState = rememberPagerState { pagesCount }

    val coroutineScope = rememberCoroutineScope()

    val viewModel: LoginViewModel = viewModel()

    LaunchedEffect(key1 = Unit) {
        if (viewModel.hasCurrentUser()) {
            val intent = Intent(activity, SecondActivity::class.java)
            activity.startActivity(intent)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(color = Color.Black), // Здесь вы можете настроить прозрачность черного цвета
        contentAlignment = Alignment.Center
    ) {
        // Фоновое изображение
        Image(
            painter = painterResource(id = R.drawable.bill),
            contentDescription = "Background image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillWidth // Заполнение по ширине экрана
        )

        // Основной контейнер с полями входа и регистрации
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Чтобы элементы были чуть выше центра
        ) {
            Spacer(modifier = Modifier.height(200.dp)) // Добавление пространства сверху
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                        contentColor = Color.White,
                containerColor = Color.Black
            ) {
                val tabs = listOf("Sign in", "Sign up")
                tabs.forEachIndexed { index, tabItem ->
                    Tab(
                        text = { Text(tabItem) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
            ) { page: Int ->

                when (page) {
                    0 -> LoginPage(viewModel, activity)
                    1 -> SignUpPage(viewModel, activity)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginPage(viewModel: LoginViewModel, activity: ComponentActivity) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val userDao = viewModel.userDao
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it.take(18)
                viewModel.username = it.take(18)
            },
            label = { Text("Username") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.White) // Установка цвета текста
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it.take(18)
                viewModel.password = it.take(18)
            },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            textStyle = TextStyle(color = Color.White) // Установка цвета текста
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            // Проверка наличия введенных данных
            if (username.isNotBlank() && password.isNotBlank()) {
                // Проверка наличия пользователя в базе данных
                val user = userDao.getUserByLogin(username)
                if (user != null && user.password == password) {
                    user.isSigned = true
                    userDao.update(user)
                    // Если пользователь найден и пароль совпадает, то пользователь авторизован
                    Toast.makeText(activity, "Успешно авторизован", Toast.LENGTH_SHORT).show()
                    val intent = Intent(activity, SecondActivity::class.java)
                    activity.startActivity(intent)
                } else {
                    // Если пользователя не найден или пароль не совпадает, выводим сообщение об ошибке
                    Toast.makeText(activity, "Неккоретный пароль или логин", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Если одно из полей не заполнено, выводим сообщение об ошибке
                Toast.makeText(activity, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Sign In")
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignUpPage(viewModel: LoginViewModel, activity: ComponentActivity) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val userDao = viewModel.userDao

    username = viewModel.username
    password = viewModel.password

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it.take(18)
                viewModel.username = it.take(18)
                 },
            label = { Text("Username") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.White) // Установка цвета текста
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it.take(18)
                viewModel.password = it.take(18)
                 },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            textStyle = TextStyle(color = Color.White) // Установка цвета текста
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it.take(18) },
            label = { Text("Confirm Password") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            textStyle = TextStyle(color = Color.White) // Установка цвета текста
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            // Проверка наличия введенных данных
            if (username.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()) {
                // Проверка совпадения пароля и подтверждения пароля
                if (password == confirmPassword) {
                    // Проверка наличия пользователя в базе данных
                    val existingUser = userDao.getUserByLogin(username)
                    if (existingUser == null) {
                        // Если пользователя нет, добавляем его в базу данных
                        viewModel.userDao.insert(User(login = username, password = password, isSigned = true))
                        // Выводим сообщение об успешной регистрации
                        Toast.makeText(activity, "Успешная регистрация", Toast.LENGTH_SHORT).show()
                        val intent = Intent(activity, SecondActivity::class.java)
                        activity.startActivity(intent)
                    } else {
                        // Если пользователь уже существует, выводим сообщение об ошибке
                        Toast.makeText(activity, "Такой пользователь уже существует", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Если поля пароля и подтверждения пароля не совпадают, выводим сообщение об ошибке
                    Toast.makeText(activity, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Если одно из полей не заполнено, выводим сообщение об ошибке
                Toast.makeText(activity, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Sign Up")
        }
    }
}
