package com.example.dzandroid1

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(user: User)

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)

    @Query("SELECT * FROM users WHERE login = :login")
    fun getUserByLogin(login: String): User?

    @Query("SELECT * from users ORDER BY login ASC")
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE is_signed = 1")
    fun getCurrentUser(): Flow<User?>
}