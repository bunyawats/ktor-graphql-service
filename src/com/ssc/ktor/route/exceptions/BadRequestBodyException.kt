package com.ssc.ktor.route.exceptions

import io.ktor.http.*

class BadRequestBodyException(
    details: String
) : FindAnotherNameException(HttpStatusCode.BadRequest, "Invalid body ", details)