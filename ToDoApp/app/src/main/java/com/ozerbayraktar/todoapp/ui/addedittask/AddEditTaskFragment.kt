package com.ozerbayraktar.todoapp.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ozerbayraktar.todoapp.R
import com.ozerbayraktar.todoapp.databinding.FragmentAddEditTaskBinding
import com.ozerbayraktar.todoapp.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject


@AndroidEntryPoint
class AddEditTaskFragment @Inject constructor(): Fragment(R.layout.fragment_add_edit_task) {

    private val viewModel: AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding =FragmentAddEditTaskBinding.bind(view)

        binding.apply {
            editTextTaskLabel.setText(viewModel.taskName)
            textviewMetin.setText(viewModel.taskText)
            checkBoxImportant.isChecked=viewModel.taskImportance
            checkBoxImportant.jumpDrawablesToCurrentState()
            textViewDateCreated.isVisible = viewModel.task != null
            textViewDateCreated.text="Created: ${viewModel.task?.createdDataFormatted}"


            editTextTaskLabel.addTextChangedListener {
                viewModel.taskName=it.toString()
            }
            textviewMetin.addTextChangedListener {
                viewModel.taskText=it.toString()
            }

            // _ mean ignore to first arg
            checkBoxImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance=isChecked
            }

            //to what happen when we click fab in edit Task Fragment
            fabSaveTask.setOnClickListener {
                viewModel.onSaveClick()
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.addEditTaskEvent.collect{event->
                when(event){
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {

                        binding.textviewMetin.clearFocus()
                        //to result back to previous fragment with snackbar
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(),event.msg, Snackbar.LENGTH_LONG).show()

                    }
                }.exhaustive

            }
        }
    }
}