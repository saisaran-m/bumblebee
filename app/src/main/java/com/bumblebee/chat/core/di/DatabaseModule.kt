package com.bumblebee.chat.core.di

import android.content.Context
import androidx.room.Room
import com.bumblebee.chat.data.local.BumblebeeDatabase
import com.bumblebee.chat.data.local.dao.ConversationDao
import com.bumblebee.chat.data.local.dao.MessageDao
import com.bumblebee.chat.data.local.dao.PeerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): BumblebeeDatabase = Room.databaseBuilder(
        context,
        BumblebeeDatabase::class.java,
        "bumblebee_database"
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideConversationDao(db: BumblebeeDatabase): ConversationDao = db.conversationDao()

    @Provides
    fun provideMessageDao(db: BumblebeeDatabase): MessageDao = db.messageDao()

    @Provides
    fun providePeerDao(db: BumblebeeDatabase): PeerDao = db.peerDao()
}
