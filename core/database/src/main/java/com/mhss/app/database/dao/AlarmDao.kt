package com.mhss.app.database.dao

import androidx.room.*
import com.mhss.app.database.entity.AlarmEntity

@Dao //Database access object
interface AlarmDao {

    @Query("SELECT * FROM alarms")
    suspend fun getAll(): List<AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity)

    @Delete //room
    suspend fun delete(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun delete(id: Int)

}