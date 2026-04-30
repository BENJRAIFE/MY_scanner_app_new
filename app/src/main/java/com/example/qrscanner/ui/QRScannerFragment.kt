package com.example.qrscanner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.example.qrscanner.R
import com.example.qrscanner.databinding.FragmentQrscannerBinding
import com.example.qrscanner.db.DbHelper
import com.example.qrscanner.db.DbHelperI
import com.example.qrscanner.db.database.QrResultDataBase
import com.example.qrscanner.ui.dialog.QrCodeResultDialog
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class QRScannerFragment : Fragment() {

    companion object {
        fun newInstance(): QRScannerFragment {
            return QRScannerFragment()
        }
    }

    private var _binding: FragmentQrscannerBinding? = null
    private val binding get() = _binding!!

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private lateinit var dbHelperI: DbHelperI
    private var isScanning = true
    private var camera: Camera? = null
    private var flashEnabled = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentQrscannerBinding.inflate(inflater, container, false)

        dbHelperI = DbHelper(QrResultDataBase.getAppDatabase(requireContext())!!)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.flashToggle.setOnClickListener {
            toggleFlash()
        }

        return binding.root
    }

    private fun toggleFlash() {
        camera?.let {
            if (it.cameraInfo.hasFlashUnit()) {
                flashEnabled = !flashEnabled
                it.cameraControl.enableTorch(flashEnabled)
                if (flashEnabled) {
                    binding.flashToggle.setImageResource(R.drawable.ic_flash_on)
                } else {
                    binding.flashToggle.setImageResource(R.drawable.ic_flash_off)
                }
            } else {
                Toast.makeText(requireContext(), "Flash not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val barcodeScanner = BarcodeScanning.getClient()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(barcodeScanner, imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImageProxy(
        barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && isScanning) {

            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->

                    for (barcode in barcodes) {
                        val value = barcode.rawValue
                        if (!value.isNullOrEmpty() && isScanning) {
                            isScanning = false
                            val id = dbHelperI.insertQRResult(value)
                            val qrResult = dbHelperI.getQRResult(id)
                            showResultDialog(qrResult)
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun showResultDialog(qrResult: com.example.qrscanner.db.entities.QrResult) {
        val dialog = QrCodeResultDialog(requireContext())
        dialog.setOnDismissListener(object : QrCodeResultDialog.OnDismissListener {
            override fun onDismiss() {
                isScanning = true
            }
        })
        dialog.show(qrResult)
    }

    private fun allPermissionsGranted() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}