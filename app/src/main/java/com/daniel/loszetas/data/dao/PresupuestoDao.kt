package com.daniel.loszetas.data.dao

import androidx.room.*
import com.daniel.loszetas.data.entities.Presupuesto
import kotlinx.coroutines.flow.Flow

@Dao
interface PresupuestoDao {

    // Agregar presupuesto
    @Insert
    suspend fun insertar(presupuesto: Presupuesto)

    // Obtener todos los presupuestos
    @Query("SELECT * FROM presupuestos ORDER BY fechaInicio DESC")
    fun obtenerTodos(): Flow<List<Presupuesto>>

    // Obtener presupuesto por categoría
    @Query("SELECT * FROM presupuestos WHERE categoria = :categoria")
    fun obtenerPorCategoria(categoria: String): Flow<List<Presupuesto>>

    // Actualizar gasto actual de un presupuesto
    @Query("UPDATE presupuestos SET gastoActual = :nuevoGasto WHERE id = :presupuestoId")
    suspend fun actualizarGasto(presupuestoId: Long, nuevoGasto: Double)

    // Eliminar presupuesto
    @Delete
    suspend fun eliminar(presupuesto: Presupuesto)
}