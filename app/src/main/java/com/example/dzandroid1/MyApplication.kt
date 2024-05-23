package com.example.dzandroid1

import android.app.Application
import androidx.room.Room

class MyApplication: Application() {
    private lateinit var db: DataBase
    lateinit var userDao: UserDao

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(applicationContext, DataBase::class.java, "db")
            .allowMainThreadQueries()
            .build()

        userDao = db.userDao()
    }

}