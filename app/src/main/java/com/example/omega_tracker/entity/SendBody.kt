package com.example.omega_tracker.entity

interface SendBody {
    val timeSpent:String
    val description:String
    val date:Long
    val stateTask:String
    val authorId:String
}