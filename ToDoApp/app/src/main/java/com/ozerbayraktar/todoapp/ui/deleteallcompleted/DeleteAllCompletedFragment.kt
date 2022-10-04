package com.ozerbayraktar.todoapp.ui.deleteallcompleted

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAllCompletedFragment : DialogFragment() {
    private val viewModel: DeleteAllCompletedViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Silmeyi Onayla")
            .setMessage("Tamamlanmış tüm notları silmek istediğinize emin misiniz")
            .setNegativeButton("İptal",null)
            .setPositiveButton("Evet", ){_,_ ->
                viewModel.onConfirmClick()
            }
            .create()


}