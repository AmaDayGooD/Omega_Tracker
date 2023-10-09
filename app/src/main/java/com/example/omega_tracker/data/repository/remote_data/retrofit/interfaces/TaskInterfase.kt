package com.example.omega_tracker.data.repository.remote_data.retrofit.interfaces

import com.example.omega_tracker.data.repository.remote_data.retrofit.dataclasses.AllData
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

interface TaskInterfase {
    @Headers("Accept: application/json")
    @GET("api/issues?fields=id,summary,description,project(name,shortName),customFields(value(minutes,name),minutes,name,id,projectCustomField(field(value())))")
    suspend fun getAllInfo(@Header("Authorization") token: String): MutableList<AllData>
}