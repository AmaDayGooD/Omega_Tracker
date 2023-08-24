package com.example.omega_tracker.retrofit.interfaces

import com.example.omega_tracker.retrofit.dataclasses.AllData
import com.example.omega_tracker.retrofit.dataclasses.EstimateTasks
import com.example.omega_tracker.retrofit.dataclasses.StartDate
import com.example.omega_tracker.retrofit.dataclasses.Tasks
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface TaskInterfase {
    @Headers("Accept: application/json")
    @GET("api/issues?fields=id,summary,description")
    suspend fun getAllTasks(@Header("Authorization") token: String): List<Tasks>

    @Headers("Accept: application/json")
    @GET("api/issues/{id}/fields/179-1?fields=id,name,value(id)")
    suspend fun getStartDate(@Header("Authorization") token: String, @Path("id") id : String): StartDate

    @Headers("Accept: application/json")
    @GET("api/issues?fields=id,summary,description,customFields(value(minutes),name,id,projectCustomField(field(value())))")
    suspend fun getAllInfo(@Header("Authorization") token: String): MutableList<AllData>
}