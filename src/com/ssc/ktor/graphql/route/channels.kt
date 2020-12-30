package com.ssc.ktor.graphql.route

import com.ssc.ktor.graphql.domain.Pageable
import com.ssc.ktor.graphql.schema.models.Channel
import com.ssc.ktor.graphql.service.TvService
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.DI
import org.kodein.di.instance

fun Route.channels(kodein: DI) {

    val tvService: TvService by kodein.instance<TvService>()

    // ../channels
    get<ChannelsLocation> { location ->
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
        call.respond(
            SuccessResp.of(
                tvService.deleteChannel(ch.id)
            )
        )
    }
}

data class ChannelRequest(
    val id: Int?,
    val title: String,
    val logo: String,
    val archived: Boolean,
    val rank: Int?
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