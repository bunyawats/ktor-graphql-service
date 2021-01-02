package com.ssc.ktor.graphql.schema.models

import com.expediagroup.graphql.annotations.GraphQLDescription


@GraphQLDescription("Contains Movie Metadata, id, title, archived and rank.")
data class Movie(
    val id: Int?,
    val title: String,
    val year: Int,
    val budget: Long,
    val channelId: Int
)