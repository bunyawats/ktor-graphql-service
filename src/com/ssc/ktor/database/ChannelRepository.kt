package com.ssc.ktor.database

import com.google.gson.JsonObject
import com.google.gson.JsonParser.parseString
import com.ssc.jooq.db.Tables.CHANNEL
import com.ssc.jooq.db.Tables.MOVIE
import com.ssc.jooq.db.tables.records.ChannelRecord
import com.ssc.ktor.graphql.schema.models.Channel
import com.ssc.ktor.service.domain.Pageable
import org.jooq.JSON
import java.util.*

class ChannelRepository constructor(private val database: Database) {

    private val channelMapper: (record: ChannelRecord) -> Channel? = { record ->

        println("\n json from mysql ${record.json} \n")
        val isArchived = record.archived > 0

        val channel = Channel(
            record.id,
            record.title,
            record.logo,
            isArchived,
            record.rank
        )

        record.json?.apply {
            channel.jsonData =
                parseString(
                    this.toString()
                ).asJsonObject
        }

        channel
    }

    suspend fun getChannels(pageable: Pageable): MutableList<Channel> {

        println(" \n in ChannelRepository.getChannels $pageable \n ")
        return database.query {
            it.selectFrom(CHANNEL)
                .orderBy(CHANNEL.RANK.desc())
                .offset(pageable.size * pageable.page)
                .limit(pageable.size)
                .fetch(channelMapper)
        }
    }

    suspend fun getChannel(id: Int): Channel? {

        println(" \n in ChannelRepository.getChannel $id \n ")
        return database.query { dsl ->
            val channel = dsl.selectFrom(CHANNEL)
                .where(CHANNEL.ID.eq(id))
                .fetchOne(channelMapper)

            channel?.apply {
                movieIds = dsl.selectFrom(MOVIE)
                    .where(MOVIE.CHANNEL_ID.eq(id))
                    .fetch(MOVIE.ID)
            }
        }
    }

    suspend fun storeChannel(channel: Channel): Channel {

        println(" \n in ChannelRepository.storeChannel $channel \n ")
        val id = database.write {
            it.newRecord(CHANNEL).apply {
                title = channel.title
                logo = channel.logo
                rank = channel.rank
                json = createDummyJSON()
            }.store()
        }
        return channel.copy(id = id)
    }

    suspend fun updateChannel(channel: Channel): Channel {

        println(" \n in ChannelRepository.updateChannel $channel \n ")
        database.write {
            it.fetchOne(
                CHANNEL,
                CHANNEL.ID.eq(channel.id)
            )?.apply {
                title = channel.title
                logo = channel.logo
                rank = channel.rank
                json = createDummyJSON()
            }?.store()
        }

        return channel
    }

    suspend fun deleteChannel(id: Int): Boolean {
        val deleted = database.write {
            it.deleteFrom(CHANNEL)
                .where(CHANNEL.ID.eq(id))
                .execute()
        }

        return deleted == 1
    }


    private fun createDummyJSON(): JSON {
        val json = JsonObject()
        json.addProperty("name", "bunyawat [---] " + Date())
        return JSON.valueOf(json.toString())
    }
}
