package com.example.omega_tracker.ui.screens.main.modelrecycleview

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.omega_tracker.R
import com.example.omega_tracker.databinding.ItemDividerBinding
import com.example.omega_tracker.databinding.ItemRunningTaskBinding

class DividerHolder(private val itemView: View, private val listener: OnItemClickListener) :
    RecyclerView.ViewHolder(itemView) {

    private val binding = ItemDividerBinding.bind(itemView)

    fun onBindView() = with(binding) {
        textShowOther.setOnClickListener {
            var result = listener.onClickItemChange()
            if (result) {
                titleDay.setText(R.string.today)
                textShowOther.setText(R.string.show_all)
            } else {
                titleDay.setText(R.string.all)
                textShowOther.setText(R.string.show_today)
            }

        }

    }
}
