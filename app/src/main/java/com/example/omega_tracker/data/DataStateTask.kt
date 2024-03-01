package com.example.omega_tracker.data

import com.example.omega_tracker.entity.StateTask

data class DataStateTask (
    override val localizedName:String,
    override val name:String,
    override val id:String,
):StateTask{
    constructor(stateTask: StateTask):this(
        stateTask.localizedName,
        stateTask.name,
        stateTask.id,
    )
}