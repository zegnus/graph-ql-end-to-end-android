package com.graphql.zegnus.graphqlapplication.api

import com.fasterxml.jackson.annotation.JsonProperty

data class Book(
    @JsonProperty("genre")
    val genre: String,
    @JsonProperty("id")
    val id: String,
    @JsonProperty("name")
    val name: String
)
