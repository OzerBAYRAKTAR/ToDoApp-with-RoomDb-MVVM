package com.ozerbayraktar.todoapp.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.ozerbayraktar.todoapp.R
import com.ozerbayraktar.todoapp.data.jetpackDatastore.SortOrder
import com.ozerbayraktar.todoapp.data.roomdb.Task
import com.ozerbayraktar.todoapp.databinding.FragmentTasksBinding
import com.ozerbayraktar.todoapp.util.exhaustive
import com.ozerbayraktar.todoapp.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_edit_task.*
import kotlinx.android.synthetic.main.fragment_tasks.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@AndroidEntryPoint
class TasksFragment:Fragment(R.layout.fragment_tasks) ,TasksAdapter.OnItemClickListener{

    private val viewModel: TasksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTasksBinding.bind(view)
        val menuHost: MenuHost = requireActivity()


        val tasksAdapter = TasksAdapter(this)

        binding.apply {
            recyclerViewTask.apply {
                adapter = tasksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task=tasksAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }

            }).attachToRecyclerView(recyclerViewTask)

            fabAddTask.setOnClickListener{
                viewModel.addNewTask()
            }

        }

        setFragmentResultListener("add_edit_request"){_,bundle ->
            val result=bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)

        }


        viewModel.tasks.observe(viewLifecycleOwner) {
            tasksAdapter.submitList(it)
        }

        //when fragment is on background we dont listen any event.
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        viewModel.tasksEvent.collect{event ->
            when(event){
                is TasksViewModel.TasksEvent.ShowUndoDeleteTaskMessage ->{
                    Snackbar.make(requireView(), "Not silindi",Snackbar.LENGTH_LONG)
                        .setAction("GERİ AL"){
                            viewModel.onUndoDeleteClick(event.task)
                        }.show()
                }
                is TasksViewModel.TasksEvent.NavigateToAddTaskScreen ->{
                    val action=TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment2("Yeni Not",null)
                    findNavController().navigate(action)
                }
                is TasksViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                    val action=TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment2("Notu Düzenle",event.task)
                    findNavController().navigate(action)

                }
                is TasksViewModel.TasksEvent.ShowTaskSavedMessage -> {
                    Snackbar.make(requireView(), event.msg,  Snackbar.LENGTH_SHORT).show()
                }
                TasksViewModel.TasksEvent.NavigateToDeleteAllCompletedScreen -> {
                    val action = TasksFragmentDirections.actionGlobalDeleteAllCompletedFragment()
                    findNavController().navigate(action)
                }
            }.exhaustive
        }
        }



        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_fragment_task, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView

                searchView.onQueryTextChanged {
                    viewModel.searchQuery.value = it

                }

                //app ilk çalıştırıldığında bu kod hide completed değerini preferences flowdan 1 kereliğine okuyacak.menuyu set edecek. ve sonra coroutine  cancel olacak.
                viewLifecycleOwner.lifecycleScope.launch{
                    menu.findItem(R.id.action_hide_completed_task).isChecked=
                        viewModel.preferencesFlow.first().hideCompleted
                }

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_sort_by_name -> {
                        viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                        return true
                    }
                    R.id.action_sort_by_date -> {
                        viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                        return true
                    }
                    R.id.action_hide_completed_task -> {
                        menuItem.isChecked= !menuItem.isChecked
                        viewModel.hideCompletedClick(menuItem.isChecked)

                        return true
                    }
                    R.id.action_delete_tasks -> {
                        viewModel.onDeleteAllCompleted()

                        return true
                    }
                }
                return false

            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, ischecked: Boolean) {
       viewModel.onTaskCheckedChange(task,ischecked)
    }
}