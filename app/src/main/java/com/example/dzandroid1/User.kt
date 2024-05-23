package com.example.dzandroid1

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["login"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "login") var login: String,
    @ColumnInfo(name = "password") var password: String,
    @ColumnInfo(name = "is_signed") var isSigned: Boolean = false
)
