package com.example.omega_tracker.ui.screens.main.modelrecycleview

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter.Config.StableIdMode
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.omega_tracker.R

class MultiViewAdapter(private val listener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private var listItem: MutableList<UiModel> = mutableListOf()
    private var listRunningTaskItem: MutableList<UiModel> = mutableListOf()

    private var dividerIndex = 0

    fun setData(newTaskList: MutableList<UiModel>) {
        listItem.addAll(newTaskList)
        sortList()
        dividerIndex = listItem.indexOfFirst { it is UiModel.DividerModel }
        notifyDataSetChanged()
    }

    fun updateListTasks(
        list: MutableList<UiModel>,
        listNotRunningTask: MutableList<UiModel> = mutableListOf()
    ) {

        dividerIndex = listItem.indexOfFirst { it is UiModel.DividerModel }

        if (dividerIndex == -1) {
            listItem.add(UiModel.DividerModel(Divider(1, false)))
            dividerIndex = listItem.size - 1
            sortList()
        }
        val listRunningTask = listItem.slice(0 until dividerIndex)
        if (listRunningTask.isNotEmpty()) {
            listRunningTaskItem.clear()
            listRunningTaskItem.addAll(listRunningTask)
        }
        val dividerModel = listItem[dividerIndex]
        val oldSize = listItem.size

        listItem = list
        listItem.add(dividerModel)
        listItem.addAll(listRunningTaskItem)
        sortList()
        dividerIndex = listItem.indexOfFirst { it is UiModel.DividerModel }

        val newSize = listItem.size

        if (oldSize > newSize) {
            val currentSize = oldSize - newSize
            notifyItemRangeRemoved(currentSize, oldSize)
            return
        }
        notifyItemRangeChanged(dividerIndex, listItem.size)
    }

    fun addDivider(dividerModel: UiModel.DividerModel) {
        listItem.add(dividerModel)
    }

    fun addRunningTask(newItem: UiModel.RunningTaskModel) {
        listItem.add(newItem)
        //  Исправить баг с повторяющимися запущенными задачами при смене темы

        sortList()
        dividerIndex = listItem.indexOfFirst { it is UiModel.DividerModel }
        notifyItemRangeChanged(0, dividerIndex)
    }

    fun removeRunningTask(itemForRemoval: UiModel.RunningTaskModel) {
        listItem.removeIf { it is UiModel.RunningTaskModel && it.runningTask.id == itemForRemoval.runningTask.id }
        sortList()
        dividerIndex = listItem.indexOfFirst { it is UiModel.DividerModel }
        notifyDataSetChanged()
    }

    fun updateRunningTask(item: UiModel.RunningTaskModel) {
        val temporaryList = listItem
        temporaryList.forEach {
            if (it is UiModel.RunningTaskModel && it.runningTask.id == item.runningTask.id) {
                val index = listItem.indexOf(it)
                (listItem[index] as UiModel.RunningTaskModel).runningTask.timeLeft =
                    item.runningTask.timeLeft
                notifyItemChanged(index)
                Log.d("MyLog", "test $listItem")
                return@forEach
            }
        }
        sortList()
        notifyItemChanged(listItem.indexOf(item))
    }

    private fun sortList() {
        listItem.sortBy {
            when (it) {
                is UiModel.RunningTaskModel -> 0
                is UiModel.DividerModel -> 1
                is UiModel.TaskModel -> 2
            }
        }
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    override fun getItemId(position: Int): Long {
        return when (val item = listItem[position]) {
            is UiModel.RunningTaskModel -> item.runningTask.id.hashCode().toLong()
            is UiModel.DividerModel -> item.divider.id
            is UiModel.TaskModel -> item.task.id.hashCode().toLong()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val t = layoutInflater.inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.item_tasks -> TaskHolder(t, listener)
            R.layout.item_running_task -> RunningTaskHolder(t, listener)
            R.layout.item_divider -> DividerHolder(t, listener)

            else -> throw IllegalArgumentException("IllegalArgumentException")
        }
    }

    override fun getItemViewType(position: Int) = when (listItem[position]) {
        is UiModel.TaskModel -> R.layout.item_tasks
        is UiModel.DividerModel -> R.layout.item_divider
        is UiModel.RunningTaskModel -> R.layout.item_running_task
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = listItem[position]
        when (holder) {
            is TaskHolder -> holder.onBindView(item as UiModel.TaskModel)
            is RunningTaskHolder -> holder.onBindView(item as UiModel.RunningTaskModel)
            is DividerHolder -> holder.onBindView(item as UiModel.DividerModel)
        }
    }

    private fun log(text: String) {
        Log.d("MyLog", "$text")
    }
}