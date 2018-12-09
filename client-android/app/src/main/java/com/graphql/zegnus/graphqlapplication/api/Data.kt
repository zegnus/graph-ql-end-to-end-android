package com.graphql.zegnus.graphqlapplication.api

import com.fasterxml.jackson.annotation.JsonProperty

data class Data(
    @JsonProperty("book")
    val book: Book
)
