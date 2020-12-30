package com.ssc.ktor.graphql.schema.models

import com.expediagroup.graphql.annotations.GraphQLDescription
import com.ssc.ktor.graphql.service.TvService

@GraphQLDescription("Contains Channel Metadata, id, title, archived and rank.")
data class Channel(
    val id: Int?,
    val title: String,
    val logo: String,
    val archived: Boolean,
    val rank: Int?
)