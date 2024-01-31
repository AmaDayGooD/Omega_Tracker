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

    fun onBindView(dividerModel: UiModel.DividerModel) = with(binding) {
        textShowOther.setOnClickListener {
            var result = listener.onClickItemChange()
            Log.d("MyLog","onBind $result")
            if (result) {
                Log.d("MyLog","now $result")
                titleDay.setText(R.string.today)
                textShowOther.setText(R.string.show_all)
            } else {
                Log.d("MyLog","all $result")
                titleDay.setText(R.string.all)
                textShowOther.setText(R.string.show_today)
            }

        }

    }
}
