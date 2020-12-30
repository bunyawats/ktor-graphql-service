package com.ssc.ktor.graphql

import com.fasterxml.jackson.databind.SerializationFeature
import com.ssc.ktor.graphql.database.Database
import com.ssc.ktor.graphql.database.FlywayFeature
import com.ssc.ktor.graphql.errors.statusPageConfiguration
import com.ssc.ktor.graphql.route.channels
import com.ssc.ktor.graphql.route.graphqlRoute
import com.ssc.ktor.graphql.route.sampleRoute
import com.ssc.ktor.graphql.schema.GraphQLHandler
import com.ssc.ktor.graphql.service.TvService
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.routing.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.provider
import org.kodein.di.singleton
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

    val database = Database(this)
    val tvService = TvService(database)

    val kodein = DI {
        bind<Database>() with singleton { database }
        bind<TvService>() with provider { tvService }
    }

    GraphQLHandler.initDI(kodein)

    install(Locations)
    install(StatusPages, statusPageConfiguration)
    install(FlywayFeature) {
        dataSource = database.connectionPool
    }
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


