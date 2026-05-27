package com.trixxwids.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetDao {
    @Query("SELECT * FROM widgets ORDER BY id DESC")
    fun getAllWidgets(): Flow<List<WidgetEntity>>

    @Query("SELECT * FROM widgets WHERE id = :widgetId")
    suspend fun getWidgetById(widgetId: Int): WidgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidget(widget: WidgetEntity): Long

    @Delete
    suspend fun deleteWidget(widget: WidgetEntity)
}
