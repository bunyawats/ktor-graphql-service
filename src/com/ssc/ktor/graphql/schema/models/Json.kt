package com.ssc.ktor.graphql.schema.models

import com.expediagroup.graphql.annotations.GraphQLDescription


@GraphQLDescription("Contains Json Metadata, name.")
data class Json(
    val name: String
)
