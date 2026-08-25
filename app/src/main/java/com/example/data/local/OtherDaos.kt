package com.example.data.local

import androidx.room.*
import com.example.data.models.AIModelConfig
import com.example.data.models.PackingItem
import com.example.data.models.TravelDocument
import com.example.data.models.TripExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface PackingItemDao {
    @Query("SELECT * FROM packing_items WHERE tripId = :tripId ORDER BY category ASC, id ASC")
    fun getPackingItemsForTrip(tripId: Long): Flow<List<PackingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackingItems(items: List<PackingItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackingItem(item: PackingItem): Long

    @Update
    suspend fun updatePackingItem(item: PackingItem)

    @Delete
    suspend fun deletePackingItem(item: PackingItem)

    @Query("DELETE FROM packing_items WHERE tripId = :tripId")
    suspend fun clearPackingForTrip(tripId: Long)
}

@Dao
interface TravelDocumentDao {
    @Query("SELECT * FROM travel_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<TravelDocument>>

    @Query("SELECT * FROM travel_documents WHERE tripId = :tripId OR tripId IS NULL ORDER BY createdAt DESC")
    fun getDocumentsForTrip(tripId: Long): Flow<List<TravelDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: TravelDocument): Long

    @Update
    suspend fun updateDocument(document: TravelDocument)

    @Delete
    suspend fun deleteDocument(document: TravelDocument)
}

@Dao
interface TripExpenseDao {
    @Query("SELECT * FROM trip_expenses WHERE tripId = :tripId ORDER BY dateMillis DESC")
    fun getExpensesForTrip(tripId: Long): Flow<List<TripExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: TripExpense): Long

    @Delete
    suspend fun deleteExpense(expense: TripExpense)
}

@Dao
interface AIModelConfigDao {
    @Query("SELECT * FROM ai_model_configs ORDER BY priority ASC")
    fun getAllConfigs(): Flow<List<AIModelConfig>>

    @Query("SELECT * FROM ai_model_configs WHERE isEnabled = 1 ORDER BY priority ASC")
    suspend fun getEnabledConfigs(): List<AIModelConfig>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigs(configs: List<AIModelConfig>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AIModelConfig)

    @Update
    suspend fun updateConfig(config: AIModelConfig)
}
