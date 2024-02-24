package com.example.omega_tracker.ui.screens.statistics

interface ListenerMyProductivity {
    fun getCurrentPosition(position: Int)

    fun addNextDayOnGraphic()
    fun addPreviewDayOnGraphic()

    fun setCurrentDay(indexCurrentDay:Int)
}