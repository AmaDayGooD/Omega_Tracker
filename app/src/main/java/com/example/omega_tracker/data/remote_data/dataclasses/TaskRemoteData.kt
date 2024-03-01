package com.example.omega_tracker.data.remote_data.dataclasses
import com.squareup.moshi.Json


data class TaskRemoteData(
    @Json(name = "customFields")
    val customFields: List<CustomField>,
    @Json(name = "summary")
    val summary: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "project")
    val project: Project,
    @Json(name = "id")
    val id: String
)

data class Project(
    @Json(name = "name")
    val name: String,
    @Json(name = "shortName")
    val shortName: String,
    @Json(name ="iconUrl")
    val iconUrl:String
)

data class CustomField(
    @Json(name = "projectCustomField")
    val projectCustomField: ProjectCustomField?,
    @Json(name = "value")
    val value: Any?,
    @Json(name = "id")
    val id: String
)

data class ProjectCustomField(
    @Json(name = "field")
    val field: Field?
)

data class Field(
    @Json(name = "name")
    val name: String?,
    @Json(name = "id")
    val id: String?
)


