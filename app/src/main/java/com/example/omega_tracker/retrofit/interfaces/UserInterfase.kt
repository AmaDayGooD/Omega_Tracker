package com.example.omega_tracker.retrofit.interfaces

import com.example.omega_tracker.retrofit.dataclasses.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

interface UserInterfase {
    @Headers(
        //"Authorization: Bearer perm:cm9vdA==.NTAtMQ==.bYTlD3WROnK9s3HuWa43iTxJPnTK7A",
        "Accept: application/json"
    )
    @GET("api/users/me?fields=name")
    suspend fun getUserOne(@Header("Authorization") token: String): Response<User>
}