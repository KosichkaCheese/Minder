package com.app.minder.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.minder.data.local.converters.TimingConverter
import com.app.minder.data.local.converters.UnitConverter
import com.app.minder.data.local.dao.*
import com.app.minder.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ProfileEntity::class,
        MedicationEntity::class,
        MedicationScheduleEntity::class,
        MedicationIntakeEntity::class,
        MeasurementTypeEntity::class,
        MeasurementGoalEntity::class,
        MeasurementEntity::class
       ],
    version = 2,
    exportSchema = false
)

@TypeConverters(UnitConverter::class, TimingConverter::class)
abstract class MedDB : RoomDatabase(){
    abstract fun userDao(): UserDao
    abstract fun profileDao(): ProfileDao
    abstract fun measurementTypeDao(): MeasurementTypeDao
    abstract fun medicationDao(): MedicationDao
    abstract fun medicationIntakeDao(): MedicationIntakeDao
    abstract fun medicationScheduleDao(): MedicationScheduleDao

    companion object{
        @Volatile
        private var INSTANCE: MedDB? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN note TEXT DEFAULT ''")
            }
        }

        fun getDB(context: Context): MedDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedDB::class.java,
                    "med_database"
                )

                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback(){
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val db = getDB(context)
                                MeasurementTypeSeeder.seedIfEmpty(db.measurementTypeDao())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}