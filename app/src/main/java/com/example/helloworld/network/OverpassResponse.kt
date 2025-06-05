package com.example.helloworld.network

data class OverpassResponse(
    val elements: List<OverpassElement>
)

data class OverpassElement(
    val id: Long,
    val lat: Double? = null,
    val lon: Double? = null,
    val nodes: List<Long>? = null,              // only present in 'way' elements
    val tags: Map<String, String>? = null,
    val type: String
)