package com.example.helloworld.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassApiService {
    @GET("interpreter")
    fun queryOverpass(
        @Query("data") query: String
    ): Call<OverpassResponse>
}