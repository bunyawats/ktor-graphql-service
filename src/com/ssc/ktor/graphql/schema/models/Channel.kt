package com.ssc.ktor.graphql.schema.models

import com.expediagroup.graphql.annotations.GraphQLDescription
import com.ssc.ktor.graphql.BATCH_MOVIE_LOADER_NAME
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.future.await

@GraphQLDescription("Contains Channel Metadata, id, title, archived and rank.")
data class Channel(
    val id: Int?,
    val title: String,
    val logo: String,
    val archived: Boolean,
    val rank: Int?,
    var movieIds: List<Int>?
) {
    suspend fun movies(dataFetchingEnvironment: DataFetchingEnvironment): List<Movie>? {

        return dataFetchingEnvironment
            .getDataLoader<List<Int>, List<Movie>>(BATCH_MOVIE_LOADER_NAME)
            .load(movieIds).await()
    }
}