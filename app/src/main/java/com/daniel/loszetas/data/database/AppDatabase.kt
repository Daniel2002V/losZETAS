package com.daniel.loszetas.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.daniel.loszetas.data.dao.MetaDao
import com.daniel.loszetas.data.dao.PresupuestoDao
import com.daniel.loszetas.data.dao.TransaccionDao
import com.daniel.loszetas.data.entities.Meta
import com.daniel.loszetas.data.entities.Presupuesto
import com.daniel.loszetas.data.entities.Transaccion

@Database(
    entities = [Transaccion::class, Meta::class, Presupuesto::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    // Los DAOs
    abstract fun transaccionDao(): TransaccionDao
    abstract fun metaDao(): MetaDao
    abstract fun presupuestoDao(): PresupuestoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "proahorro_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}