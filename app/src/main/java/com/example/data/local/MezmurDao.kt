package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MezmurDao {
    @Query("SELECT * FROM mezmur_cache ORDER BY numberInt ASC")
    fun getAllMezmurs(): Flow<List<MezmurEntity>>

    @Query("SELECT * FROM mezmur_cache WHERE isFavorite = 1 ORDER BY numberInt ASC")
    fun getFavoriteMezmurs(): Flow<List<MezmurEntity>>

    @Query("SELECT * FROM mezmur_cache WHERE category = :category ORDER BY numberInt ASC")
    fun getMezmursByCategory(category: String): Flow<List<MezmurEntity>>

    @Query("SELECT * FROM mezmur_cache WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR lyrics LIKE '%' || :query || '%' ORDER BY numberInt ASC")
    fun searchMezmurs(query: String): Flow<List<MezmurEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mezmurs: List<MezmurEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mezmur: MezmurEntity)

    @Query("UPDATE mezmur_cache SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("SELECT * FROM mezmur_cache")
    suspend fun getAllMezmursList(): List<MezmurEntity>

    @Query("SELECT COUNT(*) FROM mezmur_cache")
    suspend fun getCount(): Int

    @Query("DELETE FROM mezmur_cache WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM mezmur_cache")
    suspend fun clearAll()

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(searchQuery: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE searchQuery = :query")
    suspend fun deleteSearchQuery(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}
