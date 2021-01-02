package com.ssc.ktor.graphql.models

import com.expediagroup.graphql.annotations.GraphQLDescription
import org.dataloader.DataLoader
import java.util.concurrent.CompletableFuture


const val BATCH_MOVIE_LOADER_NAME = "BATCH_MOVIE_LOADER"


val batchMovieLoader = DataLoader<List<Long>, List<Movie>> { ids ->
    CompletableFuture.supplyAsync {

        ids.fold(mutableListOf()) { acc: MutableList<List<Movie>>, _ ->

            val matchingMovies = mutableListOf(
                Movie(
                    id = 1,
                    title = "Onc Price",
                    year = 2020,
                    budget = 5_000_000,
                    channelId = 1
                )
            )

            acc.add(matchingMovies)
            acc
        }
    }
}

@GraphQLDescription("Contains Movie Metadata, id, title, archived and rank.")
data class Movie(
    val id: Int?,
    val title: String,
    val year: Int,
    val budget: Long,
    val channelId: Int
)