package com.bumblebee.chat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bumblebee.chat.data.local.dao.ConversationDao
import com.bumblebee.chat.data.local.dao.MessageDao
import com.bumblebee.chat.data.local.dao.PeerDao
import com.bumblebee.chat.data.local.entity.ConversationEntity
import com.bumblebee.chat.data.local.entity.MessageEntity
import com.bumblebee.chat.data.local.entity.PeerEntity

@Database(
    entities = [
        PeerEntity::class,
        ConversationEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class BumblebeeDatabase : RoomDatabase() {
    abstract fun peerDao(): PeerDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
