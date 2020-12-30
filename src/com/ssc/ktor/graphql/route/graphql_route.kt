package com.ssc.ktor.graphql.route

import com.ssc.ktor.graphql.schema.GraphQLHandler
import com.ssc.ktor.graphql.service.TvService
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.DI
import org.kodein.di.instance

fun Route.graphqlRoute(kodein: DI) {

    val tvService by kodein.instance<TvService>()
    val graphQLHandler = GraphQLHandler()


    post("graphql") {
        graphQLHandler.handle(this.call)
    }

    get("playground") {
        this.call.respondText(buildPlaygroundHtml("graphql", "graphql"), ContentType.Text.Html)
    }
}

private fun buildPlaygroundHtml(graphQLEndpoint: String, subscriptionsEndpoint: String) =
    Application::class.java.classLoader.getResource("graphql-playground.html")?.readText()
        ?.replace("\${graphQLEndpoint}", graphQLEndpoint)
        ?.replace("\${subscriptionsEndpoint}", subscriptionsEndpoint)
        ?: throw IllegalStateException("graphql-playground.html cannot be found in the classpath")
