package dev.ambitionsoftware.tymeboxed.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ambitionsoftware.tymeboxed.data.db.TymeBoxedDatabase
import dev.ambitionsoftware.tymeboxed.data.db.dao.ProfileDao
import dev.ambitionsoftware.tymeboxed.data.db.dao.SessionDao
import dev.ambitionsoftware.tymeboxed.data.db.dao.TagDao
import javax.inject.Singleton

/**
 * Provides the Room database and its DAOs to the rest of the graph.
 *
 * Repositories (`ProfileRepository`, `SessionRepository`) have `@Inject
 * constructor` annotations, so Hilt can build them automatically once the
 * DAOs are available here — no extra `@Binds` module needed.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): TymeBoxedDatabase =
        Room.databaseBuilder(
            context,
            TymeBoxedDatabase::class.java,
            TymeBoxedDatabase.DB_NAME,
        )
            .fallbackToDestructiveMigration() // Phase 1 — real migrations arrive with schema v2.
            .build()

    @Provides
    fun provideProfileDao(db: TymeBoxedDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideSessionDao(db: TymeBoxedDatabase): SessionDao = db.sessionDao()

    @Provides
    fun provideTagDao(db: TymeBoxedDatabase): TagDao = db.tagDao()
}
