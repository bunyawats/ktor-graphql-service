package com.ssc.ktor.service

import com.ssc.ktor.database.ChannelRepository
import com.ssc.ktor.database.MovieRepository
import com.ssc.ktor.graphql.models.Channel
import com.ssc.ktor.graphql.models.Movie
import com.ssc.ktor.service.domain.Pageable

class TvService(
    private val channelRepo: ChannelRepository,
    private val movieRepo: MovieRepository
) {

    suspend fun getChannels(pageable: Pageable): List<Channel> {

        println(" \n in TvService.getChannels $pageable \n ")
        return channelRepo.getChannels(pageable)
    }

    suspend fun getChannel(id: Int): Channel? {

        println(" \n in TvService.getChannel $id \n ")
        return channelRepo.getChannel(id)
    }

    suspend fun storeChannel(channel: Channel): Channel {

        println(" \n in TvService.storeChannel $channel \n ")
        return channelRepo.storeChannel(channel)
    }

    suspend fun updateChannel(channel: Channel): Channel {

        println(" \n in TvService.updateChannel $channel \n ")
        return channelRepo.updateChannel(channel)
    }

    suspend fun deleteChannel(id: Int): Boolean {

        println(" \n in TvService.deleteChannel $id \n ")
        return channelRepo.deleteChannel(id)
    }


    suspend fun getMovies(): List<Movie> {

        println(" \n in TvService.getMovies \n ")
        return movieRepo.getMovies()
    }
}
