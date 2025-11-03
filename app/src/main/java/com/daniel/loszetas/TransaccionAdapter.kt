package com.daniel.loszetas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.daniel.loszetas.data.entities.Transaccion
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransaccionAdapter : RecyclerView.Adapter<TransaccionAdapter.TransaccionViewHolder>() {

    private var transacciones: List<Transaccion> = emptyList()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    private val dateFormat = SimpleDateFormat("dd MMM", Locale("es", "CL"))

    fun actualizarTransacciones(nuevasTransacciones: List<Transaccion>) {
        transacciones = nuevasTransacciones
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaccionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaccion, parent, false)
        return TransaccionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransaccionViewHolder, position: Int) {
        holder.bind(transacciones[position])
    }

    override fun getItemCount() = transacciones.size

    inner class TransaccionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoria)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        private val tvMonto: TextView = itemView.findViewById(R.id.tvMonto)

        fun bind(transaccion: Transaccion) {
            tvCategoria.text = transaccion.categoria
            tvDescripcion.text = "${transaccion.descripcion} • ${dateFormat.format(transaccion.fecha)}"

            val montoFormateado = currencyFormat.format(transaccion.monto)
            tvMonto.text = if (transaccion.esGasto) "-$montoFormateado" else "+$montoFormateado"
            tvMonto.setTextColor(
                if (transaccion.esGasto) 0xFFDC2626.toInt() else 0xFF10B981.toInt()
            )
        }
    }
}