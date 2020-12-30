/**
 * Copyright 2020 Expedia, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ssc.ktor.graphql.schema

import com.expediagroup.graphql.SchemaGeneratorConfig
import com.expediagroup.graphql.TopLevelObject
import com.expediagroup.graphql.toSchema
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ssc.ktor.graphql.schema.models.*
import com.ssc.ktor.graphql.service.TvService
import graphql.ExceptionWhileDataFetching
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import org.dataloader.DataLoaderRegistry
import java.io.IOException

data class AuthorizedContext(val authorizedUser: User? = null, var guestUUID: String? = null)

class GraphQLHandler {

    companion object {

        fun initGraphQL(tvService: TvService): GraphQL {

            val config = SchemaGeneratorConfig(supportedPackages = listOf("com.ssc.ktor.graphql.schema"))

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
    }

    private val mapper = jacksonObjectMapper()
    private val dataLoaderRegistry = DataLoaderRegistry()

    init {
        dataLoaderRegistry.register(UNIVERSITY_LOADER_NAME, batchUniversityLoader)
        dataLoaderRegistry.register(COURSE_LOADER_NAME, batchCourseLoader)
        dataLoaderRegistry.register(BATCH_BOOK_LOADER_NAME, batchBookLoader)
    }

    /**
     * Get payload from the request.
     */
    private suspend fun getPayload(request: ApplicationRequest): Map<String, Any>? {
        return try {
            mapper.readValue<Map<String, Any>>(request.call.receiveText())
        } catch (e: IOException) {
            throw IOException("Unable to parse GraphQL payload.")
        }
    }

    /**
     * Get the variables passed in the request.
     */
    private fun getVariables(payload: Map<String, *>) =
        payload.getOrElse("variables") { emptyMap<String, Any>() } as Map<String, Any>

    /**
     * Find attache user to context (authentication would go here)
     */
    private fun getContext(request: ApplicationRequest): AuthorizedContext {
        val loggedInUser = User(
            email = "fake@site.com",
            firstName = "Someone",
            lastName = "You Don't know",
            universityId = 4
        )
        return AuthorizedContext(loggedInUser)
    }

    /**
     * Get any errors and data from [executionResult].
     */
    private fun getResult(executionResult: ExecutionResult): MutableMap<String, Any> {
        val result = mutableMapOf<String, Any>()

        if (executionResult.errors.isNotEmpty()) {
            // if we encounter duplicate errors while data fetching, only include one copy
            result["errors"] = executionResult.errors.distinctBy {
                if (it is ExceptionWhileDataFetching) {
                    it.exception
                } else {
                    it
                }
            }
        }

        try {
            // if data is null, get data will fail exceptionally
            result["data"] = executionResult.getData<Any>()
        } catch (e: Exception) {
        }

        return result
    }

    /**
     * Execute a query against schema
     */
    suspend fun handle(applicationCall: ApplicationCall, graphQL: GraphQL) {
        val payload = getPayload(applicationCall.request)

        payload?.let {
            // Execute the query against the schema
            val executionResult = graphQL.executeAsync(
                ExecutionInput.Builder()
                    .query(payload["query"].toString())
                    .variables(getVariables(payload))
                    .dataLoaderRegistry(dataLoaderRegistry)
                    .context(getContext(applicationCall.request))
            ).get()
            val result = getResult(executionResult)

            // write response as json
            applicationCall.response.call.respond(mapper.writeValueAsString(result))
        }
    }
}
