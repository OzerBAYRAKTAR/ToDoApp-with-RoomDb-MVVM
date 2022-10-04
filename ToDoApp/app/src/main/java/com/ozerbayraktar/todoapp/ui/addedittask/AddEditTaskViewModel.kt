package com.ozerbayraktar.todoapp.ui.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozerbayraktar.todoapp.data.roomdb.Task
import com.ozerbayraktar.todoapp.data.roomdb.TaskDao
import com.ozerbayraktar.todoapp.ui.ADD_TASK_RESULT_OK
import com.ozerbayraktar.todoapp.ui.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
     private val state: SavedStateHandle,
    private val taskDao: TaskDao
) : ViewModel() {

    //parantez içindeki ile nav graph'ta belirtilen args adı aynı olmalı !.
    val task=state.get<Task>("task")

    var taskName=state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field=value
            state.set("taskName",value)
        }
    var taskText=state.get<String>("taskText") ?: task?.textField ?: ""
        set(value) {
            field=value
            state.set("taskText",value)
        }
    var taskImportance=state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field=value
            state.set("taskImportance",value)
        }

    private val addEditTaskChannel= Channel<AddEditTaskEvent>()
    val addEditTaskEvent=addEditTaskChannel.receiveAsFlow()


    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidInputMessage("Başlık Boş Bırakılamaz!")
            return
        }
        if (task != null) {
            val updateTask = task.copy(name = taskName, textField = taskText, important = taskImportance)
            updateTask(updateTask)
        } else {
            val newTask=Task(name = taskName, textField = taskText, important = taskImportance)
            createTask(newTask)
        }

    }

    fun updateTask(task:Task)=viewModelScope.launch {
        taskDao.update(task)
        addEditTaskChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }
    fun createTask(task: Task)=viewModelScope.launch {
        taskDao.insert(task)
        addEditTaskChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }
    fun showInvalidInputMessage(text:String) =viewModelScope.launch {
        addEditTaskChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
    }

    sealed class AddEditTaskEvent{
        data class ShowInvalidInputMessage(val msg:String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result:Int) : AddEditTaskEvent()
    }

}