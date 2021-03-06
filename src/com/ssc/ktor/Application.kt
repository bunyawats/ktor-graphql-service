package com.ssc.ktor

import com.fasterxml.jackson.databind.SerializationFeature
import com.ssc.ktor.database.ChannelRepository
import com.ssc.ktor.database.Database
import com.ssc.ktor.database.MovieRepository
import com.ssc.ktor.graphql.GraphQLHelper
import com.ssc.ktor.route.channels
import com.ssc.ktor.route.graphqlRoute
import com.ssc.ktor.route.response.statusPageConfiguration
import com.ssc.ktor.route.sampleRoute
import com.ssc.ktor.service.TvService
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.routing.*
import org.kodein.di.bind
import org.kodein.di.ktor.CallScope
import org.kodein.di.ktor.di
import org.kodein.di.provider
import org.kodein.di.scoped
import java.text.DateFormat

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

    install(Locations)
    install(StatusPages, statusPageConfiguration)
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            dateFormat = DateFormat.getDateInstance()
        }
    }

    val database = Database(this)
    val channelRepo = ChannelRepository(database)
    val movieRepo = MovieRepository(database)

    val tvService = TvService(channelRepo, movieRepo)
    val graphQLSchema = GraphQLHelper.initGraphQLSchema(tvService)
    val dataLoaderRegistry = GraphQLHelper.initDataLoaderRegistry(tvService)

    di {
        bind() from scoped(CallScope).provider { tvService }
        bind() from scoped(CallScope).provider { graphQLSchema }
        bind() from scoped(CallScope).provider { dataLoaderRegistry }
    }

    routing {
        sampleRoute()
        channels()
        graphqlRoute()
    }
}

