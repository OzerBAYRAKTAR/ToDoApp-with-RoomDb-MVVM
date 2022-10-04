package com.ozerbayraktar.todoapp.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ozerbayraktar.todoapp.data.roomdb.Task
import com.ozerbayraktar.todoapp.databinding.ItemTasksBinding

class TasksAdapter(private val listener: OnItemClickListener) : ListAdapter<Task,TasksAdapter.TaskViewHolder > (DifferentCallback()){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding=ItemTasksBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentItem=getItem(position)
        holder.bind(currentItem)
    }

   inner class TaskViewHolder(private val binding:ItemTasksBinding):RecyclerView.ViewHolder(binding.root){
        //direkt olarak TaskFragmentte de yapabilirdik fakat gereksiz işlemler memory yi gereksiz yüklerdi.
        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }
                checkboxCompleted.setOnClickListener {
                    val position=adapterPosition
                    if (position != RecyclerView.NO_POSITION){
                        val task=getItem(position)
                        listener.onCheckBoxClick(task,checkboxCompleted.isChecked)
                    }
                }
            }
        }

       fun bind(task: Task){
            binding.apply {
                checkboxCompleted.isChecked=task.completed
                textViewName.text=task.name
                textViewName.paint.isStrikeThruText=task.completed
                labelPriority.isVisible=task.important

            }
        }

    }

    interface OnItemClickListener{
        fun onItemClick(task: Task)
        fun onCheckBoxClick(task: Task,ischecked:Boolean)
    }

    class DifferentCallback:DiffUtil.ItemCallback<Task>(){
        override fun areItemsTheSame(oldItem: Task, newItem: Task):Boolean{
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task)=
            oldItem==newItem
        }

    }

