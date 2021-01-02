package com.ssc.ktor.graphql.schema

import com.expediagroup.graphql.annotations.GraphQLDescription
import com.ssc.ktor.service.TvService

class MovieQueryService(private val tvService: TvService) {

    @GraphQLDescription("Return list of movie")
    @Suppress("unused")
    suspend fun getMovieList() = tvService.getMovies()
}
