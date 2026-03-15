package com.example.qrscanner.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.example.qrscanner.R
import com.example.qrscanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setViewPager()
        setBottomViewListener()
        setViewPagerListener()
    }

    private fun setViewPager() {
        binding.viewPager.adapter = MainPagerAdapter(supportFragmentManager)
        binding.viewPager.offscreenPageLimit = 2
    }

    private fun setBottomViewListener() {
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.qrScanMenuId -> {
                    binding.viewPager.currentItem = 0
                }
                R.id.generateQrCodeMenuId  -> {
                    binding.viewPager.currentItem = 1

                }
                R.id.scannedResultMenuId -> {
                    binding.viewPager.currentItem = 2
                }
                R.id.favouriteScannedMenuId  -> {
                    binding.viewPager.currentItem = 3
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }


    private fun setViewPagerListener() {
        binding.viewPager.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.bottomNavigationView.selectedItemId = R.id.qrScanMenuId
                    }
                    1 -> {
                        binding.bottomNavigationView.selectedItemId=R.id.generateQrCodeMenuId
                    }
                    2 -> {
                        binding.bottomNavigationView.selectedItemId = R.id.scannedResultMenuId
                    }
                    3 ->{
                        binding.bottomNavigationView.selectedItemId = R.id.favouriteScannedMenuId
                    }
                }
            }
        })
    }
}