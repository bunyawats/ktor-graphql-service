package com.ssc.ktor

import com.expediagroup.graphql.SchemaGeneratorConfig
import com.expediagroup.graphql.TopLevelObject
import com.expediagroup.graphql.toSchema
import com.fasterxml.jackson.databind.SerializationFeature
import com.ssc.ktor.database.Database
import com.ssc.ktor.database.FlywayFeature
import com.ssc.ktor.graphql.*
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
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.provider
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
    val graphQL = initGraphGL(tvService)

    val kodein = DI {
        bind<TvService>() with provider { tvService }
        bind<GraphQL>() with provider { graphQL }
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
        TopLevelObject(ChannelQueryService(tvService))
    )

    val mutations = listOf(
        TopLevelObject(LoginMutationService())
    )

    val graphQLSchema = toSchema(config, queries, mutations)

    return GraphQL.newGraphQL(graphQLSchema).build()!!

}

