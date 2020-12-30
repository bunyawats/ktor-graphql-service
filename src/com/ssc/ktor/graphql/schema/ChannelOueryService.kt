package com.ssc.ktor.graphql.schema

import com.expediagroup.graphql.annotations.GraphQLDescription
import com.ssc.ktor.graphql.schema.models.Book
import com.ssc.ktor.graphql.schema.models.Channel

class ChannelOueryService {
    @GraphQLDescription("Return list of channel based on ChannelSearchParameters options")
    @Suppress("unused")
    suspend fun searchChannels(params: ChannelSearchParameters) = Channel.search(params.id)
}

data class ChannelSearchParameters(val id: Int)