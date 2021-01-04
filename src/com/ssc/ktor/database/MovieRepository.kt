package com.ssc.ktor.database

import com.ssc.jooq.db.tables.Movie.MOVIE
import com.ssc.ktor.graphql.schema.models.Movie

class MovieRepository constructor(private val database: Database) {

    suspend fun getMovies(): List<Movie> {

        println(" \n in MovieRepository.getMovies \n ")

        return database.query {
            it.selectFrom(MOVIE).fetchInto(Movie::class.java)
        }
    }
}