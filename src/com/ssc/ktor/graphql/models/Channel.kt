package com.ssc.ktor.graphql.models

import com.expediagroup.graphql.annotations.GraphQLDescription

@GraphQLDescription("Contains Channel Metadata, id, title, archived and rank.")
data class Channel(
    val id: Int?,
    val title: String,
    val logo: String,
    val archived: Boolean,
    val rank: Int?
)