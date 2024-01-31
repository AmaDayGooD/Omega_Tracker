package com.example.omega_tracker.ui.screens.startTask

data class DataForResult(
    var day: Int,
    var hour: Int,
    var minute: Int,
    var idTask:String? = null,
    var token:String? = null,
    var comment:String? = null,
    var idState:String? = null
)