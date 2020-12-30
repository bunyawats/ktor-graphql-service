package com.ssc.ktor.graphql.schema

import com.expediagroup.graphql.annotations.GraphQLDescription
import com.ssc.ktor.graphql.schema.models.Channel
import com.ssc.ktor.graphql.service.TvService
import org.kodein.di.DI
import org.kodein.di.instance

class ChannelOueryService(kodein: DI) {

    private val tvService: TvService by kodein.instance<TvService>()

    @GraphQLDescription("Return list of channel based on ChannelSearchParameters options")
    @Suppress("unused")
    suspend fun getChannel(id: Int) = tvService.getChannel(id);
}

