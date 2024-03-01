package com.example.omega_tracker.data.remote_data.dataclasses

import com.example.omega_tracker.entity.StateTask

data class StateTaskRemoteData(
    override val localizedName: String,
    override val name: String,
    override val id: String
) : StateTask {
    constructor(stateTask: StateTask) : this(
        stateTask.localizedName,
        stateTask.name,
        stateTask.id,
    )
}
