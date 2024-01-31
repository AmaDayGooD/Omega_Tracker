package com.example.omega_tracker.data.local_data

import android.provider.ContactsContract.CommonDataKinds.Email
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Profile")
data class ProfileData(
    @PrimaryKey
    val idUser:String,
    val name:String,
    val email:String,
    val avatarUrl:String
)
