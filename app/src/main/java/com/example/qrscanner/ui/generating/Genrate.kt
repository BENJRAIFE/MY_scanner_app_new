package com.example.qrscanner.ui.generating

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.qrscanner.R
import com.example.qrscanner.ui.QRScannerFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter


class Genrate : Fragment() {

    lateinit var imgQR: ImageView
    lateinit var edtTxt: EditText
    lateinit var btnGeneretor: Button
    lateinit var viewg :View

    companion object {

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
        generatingQrCode()
        return viewg

    }
    fun generatingQrCode(){
        imgQR=viewg.findViewById(R.id.imgqrCode)
        edtTxt=viewg.findViewById(R.id.enterData)
        btnGeneretor=viewg.findViewById(R.id.btn_generaate)


        btnGeneretor.setOnClickListener {

            val data = edtTxt.text.toString().trim()
            if (data.isEmpty()){
                Toast.makeText(context,"enter qr code text ", Toast.LENGTH_SHORT).show()
            }else{
                val writer= QRCodeWriter()
                try {
                    val bitmatrix=writer.encode(data, BarcodeFormat.QR_CODE,512,512)
                    val width=bitmatrix.width
                    val height=bitmatrix.height
                    val bmb= Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565)
                    for(x in 0 until width){
                        for (y in 0 until height){
                            bmb.setPixel(x,y,if(bitmatrix[x,y]) Color.BLACK else Color.WHITE)
                        }
                    }
                    imgQR.setImageBitmap(bmb)
                }catch (e: WriterException){
                    e.printStackTrace()
                }
            }
        }
    }

}