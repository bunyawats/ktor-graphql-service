package com.ssc.ktor.graphql.schema.models

import com.expediagroup.graphql.annotations.GraphQLDescription
import com.google.gson.JsonObject
import com.ssc.ktor.graphql.BATCH_MOVIE_LOADER_NAME
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.future.await

@GraphQLDescription("Contains Channel Metadata, id, title, archived and rank.")
data class Channel(
    val id: Int?,
    val title: String,
    val logo: String,
    val archived: Boolean,
    val rank: Int?
) {

    internal var movieIds: List<Int>? = null
    internal var jsonData: JsonObject? = null

    suspend fun movies(dataFetchingEnvironment: DataFetchingEnvironment): List<Movie>? {

        return dataFetchingEnvironment
            .getDataLoader<List<Int>, List<Movie>>(BATCH_MOVIE_LOADER_NAME)
            .load(movieIds).await()
    }

    fun json(dataFetchingEnvironment: DataFetchingEnvironment): Json? {

        return (jsonData?.get("name")?.asString ?: null)
            ?.let {
                Json(it)
            }
    }
}