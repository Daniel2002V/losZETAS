package com.daniel.loszetas.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.daniel.loszetas.data.dao.CategoriaDao
import com.daniel.loszetas.data.dao.MetaDao
import com.daniel.loszetas.data.dao.PresupuestoDao
import com.daniel.loszetas.data.dao.TransaccionDao
import com.daniel.loszetas.data.entities.Categoria
import com.daniel.loszetas.data.entities.Meta
import com.daniel.loszetas.data.entities.Presupuesto
import com.daniel.loszetas.data.entities.Transaccion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Transaccion::class,
        Presupuesto::class,
        Meta::class,
        Categoria::class  // NUEVA ENTIDAD
    ],
    version = 2,  // INCREMENTAR VERSIÓN
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transaccionDao(): TransaccionDao
    abstract fun presupuestoDao(): PresupuestoDao
    abstract fun metaDao(): MetaDao
    abstract fun categoriaDao(): CategoriaDao  // NUEVO DAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finanzas_database"
                )
                    .fallbackToDestructiveMigration() // Para desarrollo - eliminar en producción
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        prepoblarCategorias(database.categoriaDao())
                    }
                }
            }
        }

        private suspend fun prepoblarCategorias(categoriaDao: CategoriaDao) {
            // Categorías de GASTOS
            val categoriasGasto = listOf(
                Categoria(nombre = "Hogar", esGasto = true),
                Categoria(nombre = "Transporte", esGasto = true),
                Categoria(nombre = "Comida", esGasto = true),
                Categoria(nombre = "Entretenimiento", esGasto = true),
                Categoria(nombre = "Salud", esGasto = true),
                Categoria(nombre = "Otros", esGasto = true)
            )

            // Categorías de INGRESOS
            val categoriasIngreso = listOf(
                Categoria(nombre = "Sueldo", esGasto = false),
                Categoria(nombre = "Freelance", esGasto = false),
                Categoria(nombre = "Inversiones", esGasto = false),
                Categoria(nombre = "Reembolso", esGasto = false),
                Categoria(nombre = "Otros", esGasto = false)
            )

            categoriaDao.insertarVarias(categoriasGasto + categoriasIngreso)
        }
    }
}