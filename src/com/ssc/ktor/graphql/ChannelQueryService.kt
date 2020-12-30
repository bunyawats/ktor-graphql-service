package com.ssc.ktor.graphql

import com.expediagroup.graphql.annotations.GraphQLDescription
import com.ssc.ktor.service.TvService


class ChannelQueryService(private val tvService: TvService) {

    @GraphQLDescription("Return list of channel based on ChannelSearchParameters options")
    @Suppress("unused")
    suspend fun getChannel(id: Int) = tvService.getChannel(id)
}

