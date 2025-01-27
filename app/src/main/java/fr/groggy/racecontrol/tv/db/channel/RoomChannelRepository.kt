package fr.groggy.racecontrol.tv.db.channel

import fr.groggy.racecontrol.tv.core.channel.ChannelRepository
import fr.groggy.racecontrol.tv.db.RaceControlTvDatabase
import fr.groggy.racecontrol.tv.f1tv.*
import fr.groggy.racecontrol.tv.f1tv.F1TvBasicChannelType.Companion.F1Live
import fr.groggy.racecontrol.tv.f1tv.F1TvBasicChannelType.Companion.Data
import fr.groggy.racecontrol.tv.f1tv.F1TvBasicChannelType.Companion.PitLane
import fr.groggy.racecontrol.tv.f1tv.F1TvBasicChannelType.Companion.Tracker
import fr.groggy.racecontrol.tv.f1tv.F1TvBasicChannelType.Companion.Unknown
import fr.groggy.racecontrol.tv.f1tv.F1TvBasicChannelType.Companion.Wif
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomChannelRepository @Inject constructor(
    database: RaceControlTvDatabase
) : ChannelRepository {

    companion object {
        private const val WIF = "WIF"
        private const val F1LIVE = "F1LIVE"
        private const val PIT_LANE = "PIT_LANE"
        private const val TRACKER = "TRACKER"
        private const val DATA = "DATA"
        private const val ONBOARD = "ONBOARD"
    }

    private val dao = database.channelDao()

    override fun observe(contentId: String): Flow<List<F1TvChannel>> {
        return dao.observeByContentId(contentId)
            .map { channels -> channels.map { toChannel(it) } }
            .distinctUntilChanged()
    }

    private fun toChannel(channel: ChannelEntity): F1TvChannel {
        return if (channel.type == ONBOARD) F1TvOnboardChannel(
            channelId = channel.channelId!!,
            contentId = channel.contentId,
            name = channel.name!!,
            subTitle = channel.subTitle,
            background = channel.background,
            driver = F1TvDriverId(channel.driver!!)
        ) else F1TvBasicChannel(
            channelId = channel.channelId,
            contentId = channel.contentId,
            type = when (channel.type) {
                WIF -> Wif
                F1LIVE -> F1Live
                PIT_LANE -> PitLane
                TRACKER -> Tracker
                DATA -> Data
                else -> Unknown(channel.type, channel.name!!)
            }
        )
    }

    override suspend fun save(contentId: String, channels: List<F1TvChannel>) {
        dao.deleteOld(contentId)

        val entities = channels.mapIndexed { index, f1TvChannel -> toEntity(index, f1TvChannel) }
        dao.upsert(entities)
    }

    private fun toEntity(orderIndex: Int, channel: F1TvChannel): ChannelEntity {
        return when (channel) {
            is F1TvBasicChannel -> {
                val (type, name) = when (channel.type) {
                    Wif -> WIF to null
                    F1Live -> F1LIVE to null
                    PitLane -> PIT_LANE to null
                    Tracker -> TRACKER to null
                    Data -> DATA to null
                    is Unknown -> channel.type.type to channel.type.name
                }
                ChannelEntity(
                    id = channel.hashCode(),
                    orderIndex = orderIndex,
                    channelId = channel.channelId,
                    contentId = channel.contentId,
                    type = type,
                    name = name,
                    subTitle = "",
                    background = null,
                    driver = null
                )
            }
            is F1TvOnboardChannel -> ChannelEntity(
                id = channel.hashCode(),
                orderIndex = orderIndex,
                channelId = channel.channelId,
                contentId = channel.contentId,
                type = ONBOARD,
                name = channel.name,
                subTitle = channel.subTitle,
                background = channel.background,
                driver = channel.driver.value
            )
        }
    }

}
