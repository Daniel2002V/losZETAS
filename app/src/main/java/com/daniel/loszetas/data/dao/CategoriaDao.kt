package com.daniel.loszetas.data.dao

import androidx.room.*
import com.daniel.loszetas.data.entities.Categoria
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Insert
    suspend fun insertar(categoria: Categoria)

    @Insert
    suspend fun insertarVarias(categorias: List<Categoria>)

    @Query("SELECT * FROM categorias WHERE esGasto = 1 AND esActiva = 1 ORDER BY nombre ASC")
    fun obtenerCategoriasGasto(): Flow<List<Categoria>>

    @Query("SELECT * FROM categorias WHERE esGasto = 0 AND esActiva = 1 ORDER BY nombre ASC")
    fun obtenerCategoriasIngreso(): Flow<List<Categoria>>

    @Query("SELECT * FROM categorias WHERE esActiva = 1 ORDER BY nombre ASC")
    fun obtenerTodasCategorias(): Flow<List<Categoria>>

    @Update
    suspend fun actualizar(categoria: Categoria)

    @Delete
    suspend fun eliminar(categoria: Categoria)
}