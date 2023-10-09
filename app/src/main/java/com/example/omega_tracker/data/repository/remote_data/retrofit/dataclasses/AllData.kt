package com.example.omega_tracker.data.repository.remote_data.retrofit.dataclasses

import com.google.gson.annotations.SerializedName

data class AllData(
    val customFields: List<CustomField>,
    val summary: String,
    val description: String,
    val project: Project,
    val id: String,
    @SerializedName("\$type")
    val type: String
)
data class Project(
    val name: String,
    val shortName: String,
    @SerializedName("\$type")
    val type: String
)

data class CustomField(
    val projectCustomField: ProjectCustomField?,
    val value: Any?,
    val id: String,
    @SerializedName("\$type")
    val type: String
)

data class ProjectCustomField(
    val field: Field?,
    @SerializedName("\$type")
    val type: String
)

data class Field(
    val name: String?,
    val id: String?,
    @SerializedName("\$type")
    val type: String
)


