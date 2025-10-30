package com.daniel.loszetas.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metas")
data class Meta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nombre: String,
    val montoObjetivo: Double,
    val montoActual: Double,
    val fechaObjetivo: Long, // fecha en milisegundos
    val completada: Boolean = false
)