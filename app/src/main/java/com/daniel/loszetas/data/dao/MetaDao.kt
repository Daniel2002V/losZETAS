package com.daniel.loszetas.data.dao

import androidx.room.*
import com.daniel.loszetas.data.entities.Meta
import kotlinx.coroutines.flow.Flow

@Dao
interface MetaDao {

    // Agregar meta
    @Insert
    suspend fun insertar(meta: Meta)

    // Obtener todas las metas
    @Query("SELECT * FROM metas ORDER BY fechaObjetivo ASC")
    fun obtenerTodas(): Flow<List<Meta>>

    // Obtener metas activas (no completadas)
    @Query("SELECT * FROM metas WHERE completada = 0 ORDER BY fechaObjetivo ASC")
    fun obtenerActivas(): Flow<List<Meta>>

    // Actualizar monto de una meta
    @Query("UPDATE metas SET montoActual = :nuevoMonto WHERE id = :metaId")
    suspend fun actualizarMonto(metaId: Long, nuevoMonto: Double)

    // Marcar meta como completada
    @Query("UPDATE metas SET completada = 1 WHERE id = :metaId")
    suspend fun marcarCompletada(metaId: Long)

    // Eliminar meta
    @Delete
    suspend fun eliminar(meta: Meta)
}