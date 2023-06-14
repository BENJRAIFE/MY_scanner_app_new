package com.example.qrscanner.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.example.qrscanner.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setViewPager()
        setBottomViewListener()
        setViewPagerListener()
    }

    private fun setViewPager() {
        viewPager.adapter = MainPagerAdapter(supportFragmentManager)
        viewPager.offscreenPageLimit = 2
    }

    private fun setBottomViewListener() {
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.qrScanMenuId -> {
                    viewPager.currentItem = 0
                }
                R.id.generateQrCodeMenuId  -> {
                    viewPager.currentItem = 1

                }
                R.id.scannedResultMenuId -> {
                    viewPager.currentItem = 2
                }
                R.id.favouriteScannedMenuId  -> {
                    viewPager.currentItem = 3
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }


    private fun setViewPagerListener() {
        viewPager.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        bottomNavigationView.selectedItemId = R.id.qrScanMenuId
                    }
                    1 -> {
                        bottomNavigationView.selectedItemId=R.id.generateQrCodeMenuId
                    }
                    2 -> {
                        bottomNavigationView.selectedItemId = R.id.scannedResultMenuId
                    }
                    3 ->{
                        bottomNavigationView.selectedItemId = R.id.favouriteScannedMenuId
                    }
                }
            }
        })
    }
}