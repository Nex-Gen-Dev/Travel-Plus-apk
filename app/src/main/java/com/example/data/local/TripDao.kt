package com.example.data.local

import androidx.room.*
import com.example.data.models.ItineraryItem
import com.example.data.models.Trip
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY createdAt DESC")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: Long): Trip?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Update
    suspend fun updateTrip(trip: Trip)

    @Delete
    suspend fun deleteTrip(trip: Trip)
}

@Dao
interface ItineraryItemDao {
    @Query("SELECT * FROM itinerary_items WHERE tripId = :tripId ORDER BY dayNumber ASC, id ASC")
    fun getItemsForTrip(tripId: Long): Flow<List<ItineraryItem>>

    @Query("SELECT * FROM itinerary_items WHERE tripId = :tripId AND dayNumber = :day ORDER BY id ASC")
    fun getItemsForDay(tripId: Long, day: Int): Flow<List<ItineraryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItineraryItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItineraryItem): Long

    @Update
    suspend fun updateItem(item: ItineraryItem)

    @Delete
    suspend fun deleteItem(item: ItineraryItem)

    @Query("DELETE FROM itinerary_items WHERE tripId = :tripId")
    suspend fun clearTripItems(tripId: Long)
}
