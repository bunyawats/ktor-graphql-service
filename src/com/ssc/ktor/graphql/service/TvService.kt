package com.ssc.ktor.graphql.service

import com.ssc.ktor.graphql.database.Database
import com.ssc.ktor.graphql.schema.models.Channel
import com.ssc.ktor.graphql.domain.Pageable
import com.ssc.jooq.db.tables.Channel.CHANNEL as ChannelTable

class TvService constructor(private val database: Database) {

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

    suspend fun getChannel(id: Int): Channel {

        println(" \n in TvService.getChannel $id \n ")

        return database.query {
            it.select()
                .from(ChannelTable)
                .where(ChannelTable.ID.eq(id))
                .fetchInto(Channel::class.java)[0]
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

    fun updateChannel(channel: Channel): Channel {

        println("Channel has updated $channel")
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
