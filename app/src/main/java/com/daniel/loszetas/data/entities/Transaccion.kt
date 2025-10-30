package com.daniel.loszetas.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transacciones")
data class Transaccion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val esGasto: Boolean, // true = gasto, false = ingreso
    val monto: Double,
    val categoria: String,
    val descripcion: String,
    val fecha: Long, // fecha en milisegundos

    // Para GASTOS
    val metodoPago: String? = null,

    // Para INGRESOS
    val cuentaDestino: String? = null
)