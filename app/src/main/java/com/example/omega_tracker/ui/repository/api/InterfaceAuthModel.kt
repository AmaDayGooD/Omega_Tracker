package com.example.omega_tracker.ui.repository.api


interface InterfaceAuthModel {
    suspend fun getDataFromApi(token: String): Boolean
    suspend fun getNameUser(token: String): String
}
