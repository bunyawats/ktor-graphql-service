/*
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

package com.ssc.ktor.graphql.schema.models

import com.expediagroup.graphql.annotations.GraphQLDescription
import kotlinx.coroutines.runBlocking
import org.dataloader.DataLoader
import java.util.concurrent.CompletableFuture.supplyAsync

const val BATCH_BOOK_LOADER_NAME = "BATCH_BOOK_LOADER"

val batchBookLoader = DataLoader<List<Long>, List<Book>> { ids ->
    supplyAsync {
        val allBooks = runBlocking { Book.search(ids.flatten()).toMutableList() }
        // produce lists of results from returned books
        ids.fold(mutableListOf()) { acc: MutableList<List<Book>>, idSet ->
            val matchingBooks = allBooks.filter { idSet.contains(it.id) }
            acc.add(matchingBooks)
            acc
        }
    }
}

@GraphQLDescription("Contains Book Metadata, title, authorship, and references to product and content.")
data class Book(
    val id: Long,
    val title: String
) {
    @Suppress("unused")
    companion object {
        fun search(ids: List<Long>): List<Book> {
            return listOf(
                Book(id = 1, title = "Campbell Biology"),
                Book(id = 2, title = "The Cell"),
                Book(id = 3, title = "Data Structures in C++"),
                Book(id = 4, title = "The Algorithm Design Manual")
            ).filter { ids.contains(it.id) }
        }
    }
}
