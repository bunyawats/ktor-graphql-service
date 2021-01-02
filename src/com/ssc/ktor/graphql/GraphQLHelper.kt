package com.ssc.ktor.graphql

import com.expediagroup.graphql.SchemaGeneratorConfig
import com.expediagroup.graphql.TopLevelObject
import com.expediagroup.graphql.toSchema
import com.ssc.ktor.graphql.schema.*
import com.ssc.ktor.graphql.schema.models.*
import com.ssc.ktor.service.TvService
import graphql.schema.GraphQLSchema
import kotlinx.coroutines.runBlocking
import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import java.util.concurrent.CompletableFuture

const val BATCH_MOVIE_LOADER_NAME = "BATCH_MOVIE_LOADER"

class GraphQLHelper {

    companion object {
        fun initGraphQLSchema(tvService: TvService): GraphQLSchema {

            val config = SchemaGeneratorConfig(supportedPackages = listOf("com.ssc.ktor.graphql.schema"))

            val queries = listOf(
                TopLevelObject(HelloQueryService()),
                TopLevelObject(BookQueryService()),
                TopLevelObject(CourseQueryService()),
                TopLevelObject(UniversityQueryService()),

                TopLevelObject(ChannelQueryService(tvService)),
                TopLevelObject(MovieQueryService(tvService))
            )

            val mutations = listOf(
                TopLevelObject(LoginMutationService())
            )

            return toSchema(config, queries, mutations)
        }

        fun initDataLoaderRegistry(tvService: TvService): DataLoaderRegistry {

            val dataLoaderRegistry = DataLoaderRegistry()

            dataLoaderRegistry.register(UNIVERSITY_LOADER_NAME, batchUniversityLoader)
            dataLoaderRegistry.register(COURSE_LOADER_NAME, batchCourseLoader)
            dataLoaderRegistry.register(BATCH_BOOK_LOADER_NAME, batchBookLoader)

            val batchMovieLoader = DataLoader<List<Int>, List<Movie>> { ids ->
                CompletableFuture.supplyAsync {
                    print("\n movies ids list:  $ids")

                    runBlocking {

                        val allMovies = tvService.getMovies()

                        ids.fold(mutableListOf()) { acc: MutableList<List<Movie>>, idSet ->
                            acc.add(allMovies.filter { idSet.contains(it.id) })
                            acc
                        }

                    }

                }
            }
            dataLoaderRegistry.register(BATCH_MOVIE_LOADER_NAME, batchMovieLoader)

            return dataLoaderRegistry
        }
    }
}