package com.example.omega_tracker.ui.screens.main.modelrecycleview

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.omega_tracker.R
import com.example.omega_tracker.data.RunningTask
import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.entity.Task

class MultiViewAdapter(private val listener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private var listItem: MutableList<Any> = mutableListOf()

    private var dividerIndex = 0

    fun test(test: String = "ничего") {
        var i = 0
        log(test)
        while (i < listItem.size) {
            log("getItemId ${getItemId(i)}")
            i++
        }
        log("===================================================================")
    }

    fun removeNotRunningTask(notRunningTask: RunningTask) {

        val allRunningTask = listItem.slice(0 until dividerIndex).toMutableList()

        allRunningTask.remove(notRunningTask)

        listItem.subList(0, dividerIndex - 1).clear()
        listItem.addAll(allRunningTask)
        test("removeNotRunningTask")
        sortList()
        notifyDataSetChanged()

        log("listItem ${listItem.size}")
    }

    private var oldSize = 0
    private var newSize = 0
    fun setData(newTaskList: MutableList<Any>) {
        oldSize = listItem.size
        dividerIndex = listItem.indexOfFirst { it is Divider }

        if (dividerIndex == -1) {
            listItem.add(Divider("divider".hashCode().toLong(), true))
            dividerIndex = listItem.indexOfFirst { it is Divider }
            sortList()
        }
        if (oldSize > dividerIndex) {
            listItem.subList(
                dividerIndex + 1,
                listItem.size
            ).clear()

            listItem.addAll(newTaskList)
            log("listItem $listItem")
            sortList()
            dividerIndex = listItem.indexOfFirst { it is Divider }
            newSize = listItem.size
            //notifyDataSetChanged()
            if (oldSize < newSize) {
                notifyItemRangeRemoved(dividerIndex, listItem.size)
            } else {
                notifyItemRangeRemoved(newSize, oldSize)
            }
        }

        test("setData 3")

    }

    private fun sortList() {
        log("before $listItem")
        listItem = listItem.reversed().distinctBy {
            when (it) {
                is Task -> it.id
                is RunningTask -> it.id
                is Divider -> it.id
                else -> throw IllegalArgumentException("Unknown type")
            }
        }.reversed().toMutableList()
        log("after $listItem")


        listItem.sortBy {
            when (it) {
                is RunningTask -> 0
                is Divider -> 1
                is Task -> 2
                else -> throw IllegalArgumentException("IllegalArgumentException")
            }
        }
    }

    fun addRunningTask(runningTask: RunningTask) {
        listItem.add(runningTask)
        sortList()
        val position = listItem.indexOf(runningTask)
        notifyItemInserted(position)
    }

    fun updateRunningTask(runningTask: RunningTask) {
        var position = -1
        val temporaryList = listItem
        temporaryList.forEach {
            if (it is RunningTask && it.id == runningTask.id) {
                position = temporaryList.indexOfFirst { (it as RunningTask).id == runningTask.id }
                listItem[position] = runningTask
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    override fun getItemId(position: Int): Long {
        return when (val item = listItem[position]) {
            is RunningTask -> "${item.id}+${item.taskStatus}".hashCode().toLong()
            is Divider -> item.id
            is Task -> "${item.id}+${item.taskStatus}".hashCode().toLong()
            else -> throw IllegalArgumentException("IllegalArgumentException")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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
        is Task -> R.layout.item_tasks
        is Divider -> R.layout.item_divider
        is RunningTask -> R.layout.item_running_task
        else -> throw IllegalArgumentException("IllegalArgumentException")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItem[position]
        when (holder) {
            is TaskHolder -> holder.onBindView(item as Task)
            is RunningTaskHolder -> holder.onBindView(item as RunningTask)
            is DividerHolder -> holder.onBindView()
        }
    }

    private fun log(text: String) {
        Log.d("MyLog", "$text")
    }
}