package com.example.omega_tracker.data.remote_data.interfaces

import com.example.omega_tracker.data.remote_data.dataclasses.*
import retrofit2.Response
import retrofit2.http.*

interface YouTrackApi {
    @Headers("Accept: application/json")
    @GET("api/issues?fields=id,summary,description,project(name,shortName),customFields(value(minutes,name),minutes,name,id,projectCustomField(field(value())))")
    suspend fun getAllInfo(@Header("Authorization") token: String): MutableList<AllData>

    @Headers("Accept: application/json")
    @GET("api/admin/customFieldSettings/bundles/state/117-0/values?fields=localizedName,name,id")
    suspend fun getStateBundleElement(@Header("Authorization") token: String): List<StateBundleElement>


    @Headers("Accept: application/json")
    @GET("api/users/me?fields=name,id,avatarUrl,email")
    suspend fun getUserOne(@Header("Authorization") token: String?): UserBody

    @Headers("Accept: application/json")
    @GET("api/issues/{id}?fields=id,summary,description,project(name,shortName),customFields(value(minutes,name),minutes,name,id,projectCustomField(field(value())))")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("id") id: String?
    ): AllData

    @Headers("Accept: application/json")
    @POST("api/issues/{id}/timeTracking/workItems")
    suspend fun postTimeSpent(
        @Header("Authorization") token: String?,
        @Body trackTimeBody: TrackTimeBody,
        @Path("id") idTask: String
    )

    @Headers("Accept: application/json")
    @POST("api/issues/{id}/fields/136-2?fields=id,value(archived,avatarUrl,buildIntegration,buildLink,color(background,foreground,id),description,fullName,id,isResolved,localizedName,login,markdownText,minutes,name,presentation,ringId,text)")
    suspend fun postStateTask(
        @Header("Authorization") token: String?,
        @Body stateTask: StateTask,
        @Path("id") idTask: String
    )


}