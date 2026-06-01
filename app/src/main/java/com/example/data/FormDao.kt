package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FormDao {
    @Query("SELECT * FROM withdrawal_forms ORDER BY createdAt DESC")
    fun getAllForms(): Flow<List<WithdrawalForm>>

    @Query("SELECT * FROM withdrawal_forms WHERE id = :id LIMIT 1")
    suspend fun getFormById(id: String): WithdrawalForm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForm(form: WithdrawalForm)

    @Update
    suspend fun updateForm(form: WithdrawalForm)

    @Delete
    suspend fun deleteForm(form: WithdrawalForm)

    @Query("DELETE FROM withdrawal_forms WHERE id = :id")
    suspend fun deleteFormById(id: String)
}
