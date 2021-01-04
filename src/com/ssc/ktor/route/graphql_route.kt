package com.ssc.ktor.route

import com.ssc.ktor.graphql.GraphQLHandler
import graphql.schema.GraphQLSchema
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.dataloader.DataLoaderRegistry
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on

fun Route.graphqlRoute() {


    post("graphql") {

        val graphQLSchema by di().on(call).instance<GraphQLSchema>()
        val dataLoaderRegistry by di().on(call).instance<DataLoaderRegistry>()

        val graphQLHandler = GraphQLHandler(
            graphQLSchema,
            dataLoaderRegistry
        )
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
