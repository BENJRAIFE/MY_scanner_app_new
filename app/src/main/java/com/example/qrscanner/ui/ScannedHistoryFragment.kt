package com.example.qrscanner.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.qrscanner.R
import com.example.qrscanner.db.DbHelper
import com.example.qrscanner.db.DbHelperI
import com.example.qrscanner.db.database.QrResultDataBase
import com.example.qrscanner.db.entities.QrResult
import com.example.qrscanner.ui.adapter.ScannedResultListAdapter
import com.example.qrscanner.utils.gone
import com.example.qrscanner.utils.visible
import java.io.Serializable
import com.example.qrscanner.databinding.FragmentScannedHistoryBinding


class ScannedHistoryFragment : Fragment() {

    enum class ResultListType : Serializable {
        ALL_RESULT, FAVOURITE_RESULT
    }

    companion object {

        private const val ARGUMENT_RESULT_LIST_TYPE = "ArgumentResultType"

        fun newInstance(screenType: ResultListType): ScannedHistoryFragment {
            val bundle = Bundle()
            bundle.putSerializable(ARGUMENT_RESULT_LIST_TYPE, screenType)
            val fragment = ScannedHistoryFragment()
            fragment.arguments = bundle
            return fragment
        }
    }


    private var _binding: FragmentScannedHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelperI: DbHelperI

    private var resultListType: ResultListType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleArguments()
    }

    private fun handleArguments() {
        resultListType = arguments?.getSerializable(ARGUMENT_RESULT_LIST_TYPE) as? ResultListType
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScannedHistoryBinding.inflate(inflater, container, false)
        init()
        setSwipeRefresh()
        onClicks()
        showListOfResults()
        return binding.root
    }

    private fun init() {
        dbHelperI = DbHelper(QrResultDataBase.getAppDatabase(requireContext())!!)
        binding.layoutHeader.tvHeaderText.text = getString(R.string.recent_scanned_results)
    }

    private fun showListOfResults() {
        when (resultListType) {
            ResultListType.ALL_RESULT -> showAllResults()
            ResultListType.FAVOURITE_RESULT -> showFavouriteResults()
            else -> {}
        }
    }


    private fun showAllResults() {
        val listOfAllResult = dbHelperI.getAllQRScannedResult()
        showResults(listOfAllResult)
        binding.layoutHeader.tvHeaderText.text = getString(R.string.recent_scanned)
    }

    private fun showFavouriteResults() {
        val listOfFavouriteResult = dbHelperI.getAllFavouriteQRScannedResult()
        showResults(listOfFavouriteResult)
        binding.layoutHeader.tvHeaderText.text = getString(R.string.favourites_scanned_results)
    }


    private fun showResults(listOfQrResult: List<QrResult>) {
        if (listOfQrResult.isNotEmpty())
            initRecyclerView(listOfQrResult)
        else
            showEmptyState()
    }

    private fun initRecyclerView(listOfQrResult: List<QrResult>) {
        binding.scannedHistoryRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.scannedHistoryRecyclerView.adapter =
            ScannedResultListAdapter(dbHelperI, requireContext(), listOfQrResult.toMutableList())
        showRecyclerView()
    }

    private fun setSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            showListOfResults()
        }
    }


    private fun onClicks() {
        binding.layoutHeader.removeAll.setOnClickListener {
            showRemoveAllScannedResultDialog()
        }
    }

    private fun showRemoveAllScannedResultDialog() {
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setTitle(getString(R.string.clear_all))
            .setMessage(getString(R.string.clear_all_result))
            .setPositiveButton(getString(R.string.clear)) { _, _ ->
                clearAllRecords()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }.show()

    }

    private fun clearAllRecords() {
        when (resultListType) {
            ResultListType.ALL_RESULT -> dbHelperI.deleteAllQRScannedResult()
            ResultListType.FAVOURITE_RESULT-> dbHelperI.deleteAllFavouriteQRScannedResult()
            else -> {}
        }
        binding.scannedHistoryRecyclerView.adapter?.notifyDataSetChanged()
        showListOfResults()
    }

    private fun showRecyclerView() {
        binding.layoutHeader.removeAll.visible()
        binding.scannedHistoryRecyclerView.visible()
        binding.noResultFound.gone()
    }

    private fun showEmptyState() {
        binding.layoutHeader.removeAll.gone()
        binding.scannedHistoryRecyclerView.gone()
        binding.noResultFound.visible()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}