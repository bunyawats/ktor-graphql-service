package com.ssc.ktor

import com.expediagroup.graphql.SchemaGeneratorConfig
import com.expediagroup.graphql.TopLevelObject
import com.expediagroup.graphql.toSchema
import com.fasterxml.jackson.databind.SerializationFeature
import com.ssc.ktor.database.ChannelRepository
import com.ssc.ktor.database.Database
import com.ssc.ktor.database.MovieRepository
import com.ssc.ktor.graphql.*
import com.ssc.ktor.graphql.models.*
import com.ssc.ktor.route.BATCH_MOVIE_LOADER_NAME
import com.ssc.ktor.route.channels
import com.ssc.ktor.route.graphqlRoute
import com.ssc.ktor.route.response.statusPageConfiguration
import com.ssc.ktor.route.sampleRoute
import com.ssc.ktor.service.TvService
import freemarker.cache.ClassTemplateLoader
import graphql.GraphQL
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.provider
import java.text.DateFormat
import java.util.concurrent.CompletableFuture

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


fun Application.module(testing: Boolean = false) {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header("MyCustomHeader")
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(Authentication) {
        basic("myBasicAuth") {
            realm = "Ktor Server"
            validate { if (it.name == "test" && it.password == "password") UserIdPrincipal(it.name) else null }
        }
    }

    val database = Database(this)
    val channelRepo = ChannelRepository(database)
    val movieRepo = MovieRepository(database)
    val tvService = TvService(channelRepo, movieRepo)
    val graphQL = initGraphGL(tvService)
    val dataLoaderRegistry = initDataLoaderRegistry(tvService)

    val kodein = DI {
        bind<TvService>() with provider { tvService }
        bind<GraphQL>() with provider { graphQL }
        bind<DataLoaderRegistry>() with provider { dataLoaderRegistry }
    }

    install(Locations)
    install(StatusPages, statusPageConfiguration)
//    install(FlywayFeature) {
//        dataSource = database.connectionPool
//    }
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            dateFormat = DateFormat.getDateInstance()
        }
    }

    routing {
        sampleRoute()
        channels(kodein)
        graphqlRoute(kodein)
    }
}

fun initGraphGL(tvService: TvService): GraphQL {

    val config = SchemaGeneratorConfig(supportedPackages = listOf("com.ssc.ktor.graphql"))

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

    val graphQLSchema = toSchema(config, queries, mutations)

    return GraphQL.newGraphQL(graphQLSchema).build()!!

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
