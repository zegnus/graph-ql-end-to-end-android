package com.graphql.zegnus.graphqlapplication.api

import com.fasterxml.jackson.annotation.JsonProperty

data class ApiResponse(
    @JsonProperty("data")
    val `data`: Data
)
