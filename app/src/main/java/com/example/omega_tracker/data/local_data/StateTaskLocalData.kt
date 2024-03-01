package com.example.omega_tracker.data.local_data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.omega_tracker.entity.StateTask

@Entity(tableName = "StateTask")
data class StateTaskLocalData(
    @PrimaryKey
   override val id:String,
   override val localizedName:String,
   override val name:String,
) : StateTask {
    constructor(stateTask: StateTask) : this(
        stateTask.localizedName,
        stateTask.name,
        stateTask.id,
    )
}

