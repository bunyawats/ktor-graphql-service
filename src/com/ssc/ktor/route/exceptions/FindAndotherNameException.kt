package com.ssc.ktor.route.exceptions

import io.ktor.http.*

open class FindAnotherNameException(
    val httpStatusCode: HttpStatusCode,
    val title: String,
    val details: String
) : RuntimeException()