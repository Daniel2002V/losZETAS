package com.daniel.loszetas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daniel.loszetas.data.entities.Presupuesto
import com.daniel.loszetas.databinding.ItemPresupuestoBinding
import java.text.NumberFormat
import java.util.Locale

class PresupuestoAdapter : RecyclerView.Adapter<PresupuestoAdapter.PresupuestoViewHolder>() {

    private var presupuestos = listOf<Presupuesto>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    fun actualizarPresupuestos(nuevosPresupuestos: List<Presupuesto>) {
        presupuestos = nuevosPresupuestos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresupuestoViewHolder {
        val binding = ItemPresupuestoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PresupuestoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PresupuestoViewHolder, position: Int) {
        holder.bind(presupuestos[position])
    }

    override fun getItemCount() = presupuestos.size

    inner class PresupuestoViewHolder(private val binding: ItemPresupuestoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(presupuesto: Presupuesto) {
            binding.tvNombrePresupuesto.text = presupuesto.categoria
            binding.tvPeriodoPresupuesto.text = presupuesto.periodo

            val porcentaje = if (presupuesto.limiteTotal > 0) {
                ((presupuesto.gastoActual / presupuesto.limiteTotal) * 100).toInt()
            } else 0

            binding.progressPresupuesto.progress = porcentaje.coerceIn(0, 100)
            binding.tvProgresoPorcentaje.text = "$porcentaje%"

            binding.tvMontoPresupuesto.text = "${currencyFormat.format(presupuesto.gastoActual)} " +
                    "de ${currencyFormat.format(presupuesto.limiteTotal)}"

            val restante = presupuesto.limiteTotal - presupuesto.gastoActual
            binding.tvRestante.text = "Restante: ${currencyFormat.format(restante)}"
        }
    }
}