package fr.groggy.racecontrol.tv.db

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.groggy.racecontrol.tv.core.settings.SettingsRepository
import fr.groggy.racecontrol.tv.f1tv.F1TvChannelId
import fr.groggy.racecontrol.tv.f1tv.F1TvEventId
import fr.groggy.racecontrol.tv.f1tv.F1TvImageId
import fr.groggy.racecontrol.tv.f1tv.F1TvSessionId
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun database(@ApplicationContext context: Context): RaceControlTvDatabase =
        Room.databaseBuilder(context, RaceControlTvDatabase::class.java, "race-control-tv")
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun channelIdListMapper(): IdListMapper<F1TvChannelId> =
        IdListMapper({ it.value }, { F1TvChannelId(it) })

    @Singleton
    @Provides
    fun eventIdListMapper(): IdListMapper<F1TvEventId> =
        IdListMapper({ it.value }, { F1TvEventId(it) })

    @Singleton
    @Provides
    fun imageIdListMapper(): IdListMapper<F1TvImageId> =
        IdListMapper({ it.value }, { F1TvImageId(it) })

    @Singleton
    @Provides
    fun sessionIdListMapper(): IdListMapper<F1TvSessionId> =
        IdListMapper({ it.value }, { F1TvSessionId(it) })

    @Singleton
    @Provides
    fun settingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(
            PreferenceManager.getDefaultSharedPreferences(context)
        )
    }
}
