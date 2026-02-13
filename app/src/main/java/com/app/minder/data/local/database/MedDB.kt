package com.app.minder.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.minder.data.local.dao.UserDao
import com.app.minder.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MedDB : RoomDatabase(){
    abstract fun userDao(): UserDao

    companion object{
        @Volatile
        private var INSTANCE: MedDB? = null

        fun getDB(context: Context): MedDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedDB::class.java,
                    "med_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}