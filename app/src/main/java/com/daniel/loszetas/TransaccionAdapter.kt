package com.daniel.loszetas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.daniel.loszetas.data.entities.Transaccion
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransaccionAdapter(
    private var transacciones: List<Transaccion> = emptyList()
) : RecyclerView.Adapter<TransaccionAdapter.TransaccionViewHolder>() {

    private val dateFormat = SimpleDateFormat("d MMM", Locale("es", "ES"))
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    class TransaccionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvCategoria: TextView = view.findViewById(R.id.tvCategoria)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvMonto: TextView = view.findViewById(R.id.tvMonto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaccionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaccion, parent, false)
        return TransaccionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransaccionViewHolder, position: Int) {
        val transaccion = transacciones[position]

        // Categoría
        holder.tvCategoria.text = transaccion.categoria

        // Descripción y fecha
        val fechaStr = dateFormat.format(Date(transaccion.fecha))
        holder.tvDescripcion.text = "${transaccion.descripcion} • $fechaStr"

        // Monto con signo
        val signo = if (transaccion.esGasto) "-" else "+"
        holder.tvMonto.text = "$signo ${currencyFormat.format(transaccion.monto)}"

        // Color del monto
        val color = if (transaccion.esGasto) {
            android.graphics.Color.parseColor("#DC2626")
        } else {
            android.graphics.Color.parseColor("#16A34A")
        }
        holder.tvMonto.setTextColor(color)
    }

    override fun getItemCount() = transacciones.size

    fun actualizarTransacciones(nuevasTransacciones: List<Transaccion>) {
        transacciones = nuevasTransacciones
        notifyDataSetChanged()
    }
}