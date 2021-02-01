package com.example.garam.takemissinginfo.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface NetworkService {

    @GET("/api/v1/weakperson/{type}")
    fun benefitsRequest(
        @Path ("type") type: String,
        @Query ("benefitType") benefitType : ArrayList<String>
    ) : Call<JsonObject>

    @POST("/api/v1/weakperson/renew")
    fun benefitRenewal(
        @Body renewRequest : JsonObject
    ) : Call<JsonObject>

    @GET("/api/v1/cafeteria/{latitude}/{longitude}")
    fun soupKitchenRequest(
        @Path ("latitude") latitude : Double,
        @Path ("longitude") longitude : Double
    ) : Call<JsonObject>

    @POST("/api/v1/cafeteria/renew")
    fun soupKitchenRenewal() : Call<JsonObject>
}