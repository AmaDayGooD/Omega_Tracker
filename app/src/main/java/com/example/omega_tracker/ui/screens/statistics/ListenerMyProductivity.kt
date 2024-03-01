package com.example.omega_tracker.ui.screens.statistics

interface ListenerMyProductivity {
    fun getCurrentPosition(position: Int)

    fun addNextDayOnGraphic()
    fun addPreviewDayOnGraphic()
    fun addNextWeekOnGraphic()
    fun addPreviewWeekOnGraphic()
    fun setCurrentDay(indexCurrentDay: Int)
    fun getListDays(): List<String>
}