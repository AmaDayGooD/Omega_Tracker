package com.example.omega_tracker.ui.screens.main.modelrecycleview

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.request.target.ViewTarget
import com.example.omega_tracker.entity.Task
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou

interface OnItemClickListener {

    fun onClickRunningTask(task: Task)
    fun onClickItemChange():Boolean

    fun getGlideToVector(): GlideToVectorYou

}