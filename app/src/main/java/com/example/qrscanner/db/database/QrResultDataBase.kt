package com.example.qrscanner.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.qrscanner.db.dao.QrResultDao
import com.example.qrscanner.db.entities.QrResult

@Database(entities = [QrResult::class], version = 1,exportSchema = false)
abstract class QrResultDataBase : RoomDatabase() {
    abstract fun getQrDao(): QrResultDao

    companion object {
        private const val DB_NAME = "QrResultDatabase"
        private var qrResultDataBase: QrResultDataBase? = null
        fun getAppDatabase(context: Context): QrResultDataBase? {
            if (qrResultDataBase == null) {
                qrResultDataBase =
                    Room.databaseBuilder(context.applicationContext, QrResultDataBase::class.java, DB_NAME)
                        .allowMainThreadQueries()
                        .build()
            }
            return qrResultDataBase
        }

        fun destroyInstance() {
            qrResultDataBase = null
        }
    }
}