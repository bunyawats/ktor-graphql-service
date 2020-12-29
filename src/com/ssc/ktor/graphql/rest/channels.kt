package com.ssc.ktor.graphql.rest

import com.ssc.ktor.graphql.domain.Channel
import com.ssc.ktor.graphql.domain.Pageable
import com.ssc.ktor.graphql.service.TvService
import io.ktor.application.call
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing

fun Routing.channels(tvService: TvService) {

    get<ChannelsLocation> { location ->
        println("\n in get channels \n")
        call.respond(
            SuccessResp.of(
                tvService.getChannels(location.toPageable()).map { ChannelResponse.fromChannel(it) })
        )
    }
    post<ChannelsLocation> {
        val channelRequest = call.receive<ChannelRequest>()
        call.respond(SuccessResp.of(ChannelResponse.fromChannel(tvService.storeChannel(channelRequest.toChannel()))))
    }
    put<ChannelsLocation.ChannelLocation> { ch ->
        val channelRequest = call.receive<ChannelRequest>()
        call.respond(SuccessResp.of(ChannelResponse.fromChannel(tvService.updateChannel(channelRequest.toChannel()))))
    }
    delete<ChannelsLocation.ChannelLocation> { ch ->
        call.respond(SuccessResp.of(tvService.deleteChannel(ch.id)))
    }
}

data class ChannelRequest(val id: Int?, val title: String, val logo: String, val archived: Boolean, val rank: Int?) {
    fun toChannel() = Channel(id, title, logo, archived, rank)
}

data class ChannelResponse(val id: Int, val title: String, val logo: String, val rank: Int) {
    companion object {
        fun fromChannel(channel: Channel) = with(channel) {
            ChannelResponse(id!!, title, logo, rank!!)
        }
    }
}


@Location("/channels")
data class ChannelsLocation(val page: Int = 0, val size: Int = 20) {
    fun toPageable(): Pageable = Pageable(page = page, size = size)

    @Location("/{id}")
    data class ChannelLocation(val id: Int)
}