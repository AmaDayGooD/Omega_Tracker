package com.example.omega_tracker.ui.screens.main.modelrecycleview

interface OnItemClickListener {

    fun onClickRunningTask(item: UiModel.RunningTaskModel, position: Int)


    fun onClickItemChange():Boolean

}