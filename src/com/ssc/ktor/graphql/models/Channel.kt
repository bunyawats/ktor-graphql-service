package com.ssc.ktor.graphql.models

import com.expediagroup.graphql.annotations.GraphQLDescription
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.future.await

@GraphQLDescription("Contains Channel Metadata, id, title, archived and rank.")
data class Channel(
    val id: Int?,
    val title: String,
    val logo: String,
    val archived: Boolean,
    val rank: Int?,
//    val movieIds: List<Long>? = listOf(1, 2)
) {
    suspend fun movies(dataFetchingEnvironment: DataFetchingEnvironment): List<Movie>? {

        return dataFetchingEnvironment.getDataLoader<List<Long>, List<Movie>>(BATCH_MOVIE_LOADER_NAME)
            .load(listOf(1, 2)).await()
    }
}