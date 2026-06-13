package com.transitline.istanbul.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.transitline.istanbul.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorite ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorite ORDER BY createdAt DESC")
    suspend fun getAll(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite WHERE type = :type AND refId = :refId")
    suspend fun delete(type: String, refId: String)

    @Query("SELECT COUNT(*) > 0 FROM favorite WHERE type = :type AND refId = :refId")
    suspend fun exists(type: String, refId: String): Boolean

    @Query("DELETE FROM favorite")
    suspend fun clear()
}
