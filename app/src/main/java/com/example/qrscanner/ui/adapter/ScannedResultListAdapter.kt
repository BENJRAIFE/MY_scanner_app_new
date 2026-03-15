package com.example.qrscanner.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.qrscanner.R
import com.example.qrscanner.databinding.LayoutSingleItemQrResultBinding
import com.example.qrscanner.db.DbHelperI
import com.example.qrscanner.db.entities.QrResult
import com.example.qrscanner.ui.dialog.QrCodeResultDialog
import com.example.qrscanner.utils.gone
import com.example.qrscanner.utils.toFormattedDisplay
import com.example.qrscanner.utils.visible

class ScannedResultListAdapter(
    var dbHelperI: DbHelperI,
    var context: Context,
    private var listOfScannedResult: MutableList<QrResult>
) : RecyclerView.Adapter<ScannedResultListAdapter.ScannedResultListViewHolder>() {

    private var resultDialog: QrCodeResultDialog = QrCodeResultDialog(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedResultListViewHolder {
        val binding = LayoutSingleItemQrResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScannedResultListViewHolder(binding)
    }

    override fun getItemCount(): Int = listOfScannedResult.size

    override fun onBindViewHolder(holder: ScannedResultListViewHolder, position: Int) {
        holder.bind(listOfScannedResult[position], position)
    }

    inner class ScannedResultListViewHolder(
        private val binding: LayoutSingleItemQrResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(qrResult: QrResult, position: Int) {

            binding.result.text = qrResult.result ?: ""
            binding.tvTime.text = qrResult.calendar.toFormattedDisplay()

            setFavourite(qrResult.favourite)
            onClicks(qrResult, position)
        }

        private fun setFavourite(isFavourite: Boolean) {
            if (isFavourite)
                binding.favouriteIcon.visible()
            else
                binding.favouriteIcon.gone()
        }

        private fun onClicks(qrResult: QrResult, position: Int) {

            binding.root.setOnClickListener {
                resultDialog.show(qrResult)
            }

            binding.root.setOnLongClickListener {
                showDeleteDialog(qrResult, position)
                true
            }
        }

        private fun showDeleteDialog(qrResult: QrResult, position: Int) {
            AlertDialog.Builder(context, R.style.CustomAlertDialog)
                .setTitle(context.getString(R.string.delete))
                .setMessage(context.getString(R.string.want_to_delete))
                .setPositiveButton(context.getString(R.string.delete)) { _, _ ->
                    deleteThisRecord(qrResult, position)
                }
                .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }

        private fun deleteThisRecord(qrResult: QrResult, position: Int) {
            dbHelperI.deleteQrResult(qrResult.id!!)
            listOfScannedResult.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}