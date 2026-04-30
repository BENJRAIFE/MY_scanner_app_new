package com.example.qrscanner.ui.generating


import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.qrscanner.R
import com.example.qrscanner.db.DbHelper
import com.example.qrscanner.db.DbHelperI
import com.example.qrscanner.db.database.QrResultDataBase
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream
import java.io.OutputStream


class Genrate : Fragment() {

    lateinit var imgQR: ImageView
    lateinit var edtTxt: EditText
    lateinit var btnGeneretor: Button
    lateinit var viewg :View
    lateinit var btn_save: Button
    lateinit var btn_share: Button
    lateinit var bmb:Bitmap
    private lateinit var dbHelperI: DbHelperI

    companion object {

         const val REQUEST_CODE=1
        fun newInstance(): Genrate {
            return Genrate()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewg= inflater.inflate(R.layout.activity_generate2, container, false)
        dbHelperI = DbHelper(QrResultDataBase.getAppDatabase(requireContext())!!)
        generatingQrCode()
        return viewg


    }
    fun generatingQrCode(){
        imgQR=viewg.findViewById(R.id.imgqrCode)
        edtTxt=viewg.findViewById(R.id.enterData)
        btnGeneretor=viewg.findViewById(R.id.btn_generaate)

        btn_save=viewg.findViewById(R.id.save_qr)
        btn_save.visibility=View.INVISIBLE
        btn_share=viewg.findViewById(R.id.share_qr)
        btn_share.visibility=View.INVISIBLE

        btnGeneretor.setOnClickListener {

            val data = edtTxt.text.toString().trim()
            if (data.isEmpty()){
                Toast.makeText(context,"enter qr code text ", Toast.LENGTH_SHORT).show()
            }else{
                val writer= QRCodeWriter()
                try {
                    val bitmatrix :BitMatrix =writer.encode(data, BarcodeFormat.QR_CODE,512,512)
                    val width=bitmatrix.width
                    val height=bitmatrix.height
                     bmb= Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565)
                    for(x in 0 until width){
                        for (y in 0 until height){
                            bmb.setPixel(x,y,if(bitmatrix[x,y]) Color.BLACK else Color.WHITE)
                        }
                    }
                    imgQR.setImageBitmap(bmb)
                    btn_share.visibility=View.VISIBLE
                    btn_save.visibility=View.VISIBLE
                    
                    // Save to database when generated
                    dbHelperI.insertQRResult(data)

                    btn_save.setOnClickListener {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            saveImage()
                        } else if (ContextCompat.checkSelfPermission(requireContext(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            saveImage()
                        } else {
                            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
                        }
                    }
                    btn_share.setOnClickListener {
                        shareImage()
                    }

                }catch (e: WriterException){
                    e.printStackTrace()
                }
            }

        }
    }

    private fun saveImage() {
        val resolver = context?.contentResolver ?: return
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "QR_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRScanner")
            }
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri != null) {
            try {
                val outputStream: OutputStream? = resolver.openOutputStream(imageUri)
                outputStream?.use {
                    bmb.compress(Bitmap.CompressFormat.JPEG, 90, it)
                }
                Toast.makeText(context, "Image Saved to Gallery", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                saveImage()
            }else{
                Toast.makeText(context,"Please provide required permission",Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    fun shareImage(){
        val bytes = ByteArrayOutputStream()
        bmb.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(context?.contentResolver, bmb, "QR Result", null) ?: return
        val imageUri = Uri.parse(path)
        
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(share, "Share QR Code"))
    }
}










