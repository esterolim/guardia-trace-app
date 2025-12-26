package com.example.guardiantrace.di

import android.content.Context
import androidx.room.Room
import com.example.guardiantrace.data.local.dao.AttachmentDao
import com.example.guardiantrace.data.local.dao.EmergencyContactDao
import com.example.guardiantrace.data.local.dao.IncidentDao
import com.example.guardiantrace.data.local.database.GuardianTraceDatabase
import com.example.guardiantrace.data.security.DatabaseKeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {
    @Provides
    @Singleton
    fun provideDatabaseKeyProvider(
        @ApplicationContext context: Context
    ): DatabaseKeyProvider {
        return DatabaseKeyProvider(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyProvider: DatabaseKeyProvider
    ): GuardianTraceDatabase {
        // Obtain passphrase from secure storage (encrypted shared preferences)
        val passphrase = SQLiteDatabase.getBytes(keyProvider.getDatabasePassphrase().toCharArray())
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            GuardianTraceDatabase::class.java,
            GuardianTraceDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideIncidentDao(database: GuardianTraceDatabase): IncidentDao {
        return database.incidentDao()
    }

    @Provides
    @Singleton
    fun provideAttachmentDao(database: GuardianTraceDatabase): AttachmentDao {
        return database.attachmentDao()
    }

    @Provides
    @Singleton
    fun provideEmergencyContactDao(database: GuardianTraceDatabase): EmergencyContactDao {
        return database.emergencyContactDao()
    }


}