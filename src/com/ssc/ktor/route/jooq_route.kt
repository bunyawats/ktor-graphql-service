package com.ssc.ktor.route

import com.ssc.ktor.graphql.schema.models.Channel
import com.ssc.ktor.graphql.schema.models.Movie
import com.ssc.ktor.service.TvService
import com.ssc.ktor.service.domain.Pageable
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on

fun Route.channels() {

    // ../channels
    get<ChannelsLocation> { location ->

        val tvService by di().on(call).instance<TvService>()
        call.respond(
            SuccessResp.of(
                tvService.getChannels(
                    location.toPageable()
                ).map {
                    ChannelResponse.fromChannel(it)
                }
            )
        )
    }

    post<ChannelsLocation> {

        val channelRequest = call.receive<ChannelRequest>()
        val tvService by di().on(call).instance<TvService>()
        println(" post Route.channels $channelRequest \n ")
        call.respond(
            SuccessResp.of(
                ChannelResponse.fromChannel(
                    tvService.storeChannel(
                        channelRequest.toChannel()
                    )
                )
            )
        )
    }

    put<ChannelsLocation.ChannelLocation> { ch ->

        val channelRequest = call.receive<ChannelRequest>()
        val tvService by di().on(call).instance<TvService>()
        call.respond(
            SuccessResp.of(
                ChannelResponse.fromChannel(
                    tvService.updateChannel(
                        channelRequest.toChannel()
                    )
                )
            )
        )
    }

    delete<ChannelsLocation.ChannelLocation> { ch ->

        val tvService by di().on(call).instance<TvService>()
        call.respond(
            SuccessResp.of(
                tvService.deleteChannel(ch.id)
            )
        )
    }

    // ../movies
    get<MoviesLocation> { location ->

        val tvService by di().on(call).instance<TvService>()
        call.respond(
            SuccessResp.of(
                tvService.getMovies().map {
                    MovieResponse.fromMovieModel(it)
                }
            )
        )
    }
}

data class ChannelRequest(
    val id: Int?,
    val title: String,
    val logo: String,
    val archived: Boolean,
    val rank: Int?,
) {

    fun toChannel() = Channel(id, title, logo, archived, rank)
}

data class ChannelResponse(
    val id: Int,
    val title: String,
    val logo: String,
    val rank: Int
) {

    companion object {
        fun fromChannel(channel: Channel) = with(channel) {
            ChannelResponse(id!!, title, logo, rank!!)
        }
    }
}


@Location("/channels")
data class ChannelsLocation(
    val page: Int = 0,
    val size: Int = 20
) {

    fun toPageable(): Pageable = Pageable(page = page, size = size)

    @Location("/{id}")
    data class ChannelLocation(val id: Int)
}

@Location("movies")
class MoviesLocation

data class MovieResponse(
    val id: Int?,
    val title: String,
    val year: Int,
    val budget: Long,
    val channelId: Int?
) {

    companion object {
        fun fromMovieModel(movie: Movie) = with(movie) {
            MovieResponse(id!!, title, year, budget, channelId)
        }
    }
}