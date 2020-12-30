package com.ssc.ktor.graphql.schema.models

import com.expediagroup.graphql.annotations.GraphQLDescription

@GraphQLDescription("Contains Channel Metadata, id, title, archived and rank.")
data class Channel(
    val id: Int?,
    val title: String,
    val logo: String,
    val archived: Boolean,
    val rank: Int?
    ) {

    @Suppress("unused")
    companion object {
        fun search(id: Int): Channel {
            return Channel(
                id = 20,
                title = "Kotlin",
                logo = "Kotlon Logo",
                archived = false,
                rank = 30
            )
        }
    }
}