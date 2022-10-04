package com.ozerbayraktar.todoapp.ui.tasks


import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ozerbayraktar.todoapp.data.jetpackDatastore.PreferencesManager
import com.ozerbayraktar.todoapp.data.jetpackDatastore.SortOrder
import com.ozerbayraktar.todoapp.data.roomdb.Task
import com.ozerbayraktar.todoapp.data.roomdb.TaskDao
import com.ozerbayraktar.todoapp.ui.ADD_TASK_RESULT_OK
import com.ozerbayraktar.todoapp.ui.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager
) : ViewModel(){

    val searchQuery= MutableStateFlow("")

    val preferencesFlow=preferencesManager.preferencesFlow

    private val tasksEventChannel= Channel<TasksEvent> ()
    //turn this to flow then we can use on fragment to get values of it
    val tasksEvent=tasksEventChannel.receiveAsFlow()

    /* val sortOrder= MutableStateFlow(SortOrder.BY_DATE)
    val hideCompleted= MutableStateFlow(false)

     */

    //flamapLatest ile her yeni arama yapıldığında son değeri döndürür
    //when any of them change, the all three combine parameters will update.
    private val taskFlow= combine(
        searchQuery,
        preferencesFlow
    ){query,filterPreferences ->
        Pair(query,filterPreferences)

    }.flatMapLatest { (query,filterPreferences) ->
        taskDao.getTasks(query,filterPreferences.sortOrder,filterPreferences.hideCompleted)
    }

    val tasks=taskFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder)=viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)

    }
    fun hideCompletedClick(hideCompleted:Boolean)=viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckedChange(task: Task,isChecked:Boolean)=viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }

    fun onTaskSwiped(task: Task)=viewModelScope.launch {
        taskDao.delete(task)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task)=viewModelScope.launch {
        taskDao.insert(task)
    }
    fun addNewTask() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }
    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedMessage("Not Eklendi")
            EDIT_TASK_RESULT_OK -> showTaskSavedMessage("Not Güncellendi")
        }
    }
    private fun showTaskSavedMessage(text:String)=viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowTaskSavedMessage(text))
    }

     fun onDeleteAllCompleted() =viewModelScope.launch {
         tasksEventChannel.send(TasksEvent.NavigateToDeleteAllCompletedScreen)
     }

    //represent kind of events that we can able use in fragment
    sealed class TasksEvent{
        object NavigateToAddTaskScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task): TasksEvent()
        data class ShowTaskSavedMessage(val msg:String) : TasksEvent()
        object NavigateToDeleteAllCompletedScreen : TasksEvent()
    }

}



