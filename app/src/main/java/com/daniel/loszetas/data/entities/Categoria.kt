package com.daniel.loszetas.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias")
data class Categoria(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nombre: String,
    val esGasto: Boolean, // true = categoría de gasto, false = categoría de ingreso
    val esActiva: Boolean = true // Para poder "eliminar" sin borrar de BD
)