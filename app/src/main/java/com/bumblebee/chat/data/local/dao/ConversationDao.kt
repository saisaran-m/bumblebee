package com.bumblebee.chat.data.local.dao

import androidx.room.*
import com.bumblebee.chat.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations ORDER BY lastMessageTimestamp DESC")
    suspend fun getAllConversationsSync(): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE conversationId = :id")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE peerId = :peerId AND isHive = 0")
    suspend fun getConversationByPeerId(peerId: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE peerId = :peerId AND isHive = 0")
    suspend fun getAllConversationsByPeerId(peerId: String): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE isHive = 1")
    fun getHives(): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(conversation: ConversationEntity)

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Delete
    suspend fun delete(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE conversationId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM conversations WHERE conversationId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE conversationId = :id")
    suspend fun clearUnreadCount(id: String)

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}
