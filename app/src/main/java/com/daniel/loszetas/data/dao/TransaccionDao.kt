package com.daniel.loszetas.data.dao

import androidx.room.*
import com.daniel.loszetas.data.entities.Transaccion
import kotlinx.coroutines.flow.Flow

@Dao
interface TransaccionDao {

    // Agregar transacción
    @Insert
    suspend fun insertar(transaccion: Transaccion)

    // Obtener todas las transacciones
    @Query("SELECT * FROM transacciones ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<Transaccion>>

    // Obtener solo gastos
    @Query("SELECT * FROM transacciones WHERE esGasto = 1 ORDER BY fecha DESC")
    fun obtenerGastos(): Flow<List<Transaccion>>

    // Obtener solo ingresos
    @Query("SELECT * FROM transacciones WHERE esGasto = 0 ORDER BY fecha DESC")
    fun obtenerIngresos(): Flow<List<Transaccion>>

    // Calcular total de gastos
    @Query("SELECT SUM(monto) FROM transacciones WHERE esGasto = 1")
    fun totalGastos(): Flow<Double?>

    // Calcular total de ingresos
    @Query("SELECT SUM(monto) FROM transacciones WHERE esGasto = 0")
    fun totalIngresos(): Flow<Double?>

    // Eliminar transacción
    @Delete
    suspend fun eliminar(transaccion: Transaccion)
}