package com.daniel.loszetas.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presupuestos")
data class Presupuesto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val categoria: String,
    val limiteTotal: Double,
    val gastoActual: Double = 0.0,
    val periodo: String, // "SEMANAL", "MENSUAL", "ANUAL"
    val fechaInicio: Long // fecha en milisegundos
)