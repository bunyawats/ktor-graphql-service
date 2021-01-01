package com.ssc.ktor.database

import com.ssc.ktor.graphql.models.Channel
import com.ssc.ktor.service.domain.Pageable
import com.ssc.jooq.db.tables.Channel.CHANNEL as ChannelTable

class ChannelRepository constructor(private val database: Database) {

    suspend fun getChannels(pageable: Pageable): List<Channel> {

        println(" \n in TvService.getChannels $pageable \n ")

        return database.query {
            it.select()
                .from(ChannelTable)
                .orderBy(ChannelTable.RANK.desc())
                .offset(pageable.size * pageable.page)
                .limit(pageable.size)
                .fetchInto(Channel::class.java)
        }
    }

    suspend fun getChannel(id: Int): Channel? {

        println(" \n in TvService.getChannel $id \n ")

        return database.query {
            it.select()
                .from(ChannelTable)
                .where(ChannelTable.ID.eq(id))
                .fetchOneInto(Channel::class.java)
        }
    }

    suspend fun storeChannel(channel: Channel): Channel {

        println(" \n in TvService.storeChannel $channel \n ")

        val id = database.write {
            it.newRecord(ChannelTable)
                .apply {
                    title = channel.title
                    logo = channel.logo
                    rank = channel.rank
                }
                .store()
        }
        return channel.copy(id = id)
    }

    suspend fun updateChannel(channel: Channel): Channel {

        println(" \n in TvService.updateChannel $channel \n ")

        database.write {

            it.fetchOne(
                ChannelTable,
                ChannelTable.ID.eq(channel.id)
            )?.apply {
                title = channel.title
                logo = channel.logo
                rank = channel.rank
            }?.store()

        }

        return channel
    }

    suspend fun deleteChannel(id: Int): Boolean {
        val deleted = database.write {
            it.deleteFrom(ChannelTable)
                .where(ChannelTable.ID.eq(id))
                .execute()
        }
        return deleted == 1
    }
}
