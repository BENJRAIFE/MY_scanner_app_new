package com.example.qrscanner.ui.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.ClipboardManager
import android.util.Log
import android.widget.Toast
import android.widget.TextView
import android.widget.ImageView
import com.example.qrscanner.R
import com.example.qrscanner.db.DbHelper
import com.example.qrscanner.db.DbHelperI
import com.example.qrscanner.db.database.QrResultDataBase
import com.example.qrscanner.db.entities.QrResult
import com.example.qrscanner.utils.ContentChekUtil.isWebUrl
import com.example.qrscanner.utils.toFormattedDisplay

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.json.JSONObject
import java.net.URL

class QrCodeResultDialog(var context: Context) {

    private lateinit var dialog: Dialog

    private lateinit var dbHelperI: DbHelperI

    private var qrResult: QrResult? = null

    private var onDismissListener: OnDismissListener? = null
    private lateinit var favouriteIcon: ImageView
    private lateinit var copyResult: ImageView
    private lateinit var shareResult: ImageView
    private lateinit var cancelDialog: ImageView
    private lateinit var scannedText: TextView
    private lateinit var scannedDate: TextView
    private lateinit var userInfo1: TextView
    private lateinit var userInfo2: TextView
    private lateinit var userInfo3: TextView
    private lateinit var userInfo4: TextView

    init {
        init()
        initDialog()
    }


    private fun init() {
        dbHelperI = DbHelper(QrResultDataBase.getAppDatabase(context)!!)
    }

    private fun initDialog() {
        dialog = Dialog(context)
        dialog.setContentView(R.layout.layout_qr_result_show)
        dialog.setCancelable(false)
        favouriteIcon = dialog.findViewById(R.id.favouriteIcon)
        copyResult = dialog.findViewById(R.id.copyResult)
        shareResult = dialog.findViewById(R.id.shareResult)
        cancelDialog = dialog.findViewById(R.id.cancelDialog)
        scannedText = dialog.findViewById(R.id.scannedText)
        scannedDate = dialog.findViewById(R.id.scannedDate)
        userInfo1 = dialog.findViewById(R.id.userInfo1)
        userInfo2 = dialog.findViewById(R.id.userInfo2)
        userInfo3 = dialog.findViewById(R.id.userInfo3)
        userInfo4 = dialog.findViewById(R.id.userInfo4)
        onClicks()
    }

    private fun onClicks() {

        favouriteIcon.setOnClickListener {
            if (it.isSelected) {
                removeFromFavourite()
            } else
                addToFavourite()
        }

        copyResult.setOnClickListener {
            copyResultToClipBoard()
        }

        shareResult.setOnClickListener {
            shareResult()
        }

        cancelDialog.setOnClickListener {
            dialog.dismiss()
            onDismissListener?.onDismiss()
        }

        scannedText.setOnClickListener {
            checkContentAndPerformAction(scannedText.text.toString())
        }
    }


    private fun addToFavourite() {
        favouriteIcon.isSelected = true
        dbHelperI.addToFavourite(qrResult?.id!!)
    }

    private fun removeFromFavourite() {
        favouriteIcon.isSelected = false
        dbHelperI.removeFromFavourite(qrResult?.id!!)
    }


    fun show(recentQrResult: QrResult) {
        this.qrResult = recentQrResult
        scannedDate.text = qrResult?.calendar?.toFormattedDisplay()
        scannedText.text = "" + qrResult!!.result
        favouriteIcon.isSelected = qrResult!!.favourite
        dialog.show()

        // Fetch Qr code Data via api
        val url = "https://qr-scanner-api.herokuapp.com/api/user/" + qrResult!!.result
        userInfo1.text = ""
        userInfo2.text = "";//context.getString(R.string.loading);
        userInfo3.text = ""
        userInfo4.text = ""

        // Run in another thread until completion to avoid thread blocking
        CoroutineScope(Dispatchers.IO).launch{
            val json = URL(url).readText()
            withContext(Dispatchers.Main) {
                val obj = JSONObject(json)

                try {

                    if (obj.getString("status") == "success"){

                        userInfo1.text = "Name: \n" + obj.getJSONObject("data").getString("name")
                        userInfo2.text = "Email: \n" + obj.getJSONObject("data").getString("email")
                        userInfo3.text = "Field: \n" + obj.getJSONObject("data").getString("field")
                        userInfo4.text = "Website: \n" + obj.getJSONObject("data").getString("website")
                    }
                    else if (obj.getString("status") == "fail"){

                        userInfo1.text = ""
                        userInfo2.text = obj.getString("message")
                        userInfo3.text = ""
                        userInfo4.text = ""
                    } else {

                    }
                }
                catch (e: NumberFormatException) {

                    Log.e("Tag", e.toString())
                }
            }
        }

    }

    fun setOnDismissListener(dismissListener: OnDismissListener) {
        this.onDismissListener = dismissListener
    }

    private fun copyResultToClipBoard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("QrCodeScannedResult", scannedText.text)
        clipboard.text = clip.getItemAt(0).text.toString()
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()

    }

    private fun shareResult() {
        val txtIntent = Intent(Intent.ACTION_SEND)
        txtIntent.type = "text/plain"
        txtIntent.putExtra(
            Intent.EXTRA_TEXT,
            scannedText.text.toString()
        )
        context.startActivity(Intent.createChooser(txtIntent, "Share QR Result"))
    }

    interface OnDismissListener {
        fun onDismiss()
    }

    // Checking content type and performing action on it.
    private fun checkContentAndPerformAction(scannedText: String) {
        when {

            // if it is web url
            isWebUrl(scannedText) -> {

                // opening web url.
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(scannedText)
                context.startActivity(i)
            }
        }
    }
}