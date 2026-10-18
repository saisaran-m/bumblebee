package com.bumblebee.chat.data.local.dao

import androidx.room.*
import com.bumblebee.chat.data.local.entity.PeerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerDao {
    @Query("SELECT * FROM peers ORDER BY lastSeen DESC")
    fun getAllPeers(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers WHERE isConnected = 1 ORDER BY displayName ASC")
    fun getConnectedPeers(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers WHERE isNearby = 1 ORDER BY displayName ASC")
    fun getNearbyPeers(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers WHERE peerId = :peerId")
    suspend fun getPeerById(peerId: String): PeerEntity?

    @Query("SELECT * FROM peers WHERE endpointId = :endpointId")
    suspend fun getPeerByEndpointId(endpointId: String): PeerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(peer: PeerEntity)

    @Update
    suspend fun update(peer: PeerEntity)

    @Delete
    suspend fun delete(peer: PeerEntity)

    @Query("UPDATE peers SET isConnected = 0, isNearby = 0, endpointId = NULL")
    suspend fun disconnectAll()

    @Query("UPDATE peers SET isConnected = 0, isNearby = 0, endpointId = NULL WHERE endpointId = :endpointId")
    suspend fun disconnectByEndpoint(endpointId: String)

    @Query("DELETE FROM peers")
    suspend fun deleteAll()
}
