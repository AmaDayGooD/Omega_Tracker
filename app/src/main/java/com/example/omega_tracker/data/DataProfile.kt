package com.example.omega_tracker.data

import android.net.Uri
import com.example.omega_tracker.entity.Profile
import com.squareup.moshi.JsonClass


data class DataProfile(
    override var name:String,
    override var id:String,
    override var email:String,
    var avatarUrl:String
):Profile{
    constructor(profile: Profile):this(
        profile.name,
        profile.id,
        profile.email,
        ""
    )

    override var avatar: Uri
        get() = Uri.parse(avatarUrl)
        set(value) {}
}