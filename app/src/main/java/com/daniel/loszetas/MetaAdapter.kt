package com.daniel.loszetas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daniel.loszetas.data.entities.Meta
import com.daniel.loszetas.databinding.ItemMetaBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class MetaAdapter : RecyclerView.Adapter<MetaAdapter.MetaViewHolder>() {

    private var metas = listOf<Meta>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun actualizarMetas(nuevasMetas: List<Meta>) {
        metas = nuevasMetas
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetaViewHolder {
        val binding = ItemMetaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MetaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MetaViewHolder, position: Int) {
        holder.bind(metas[position])
    }

    override fun getItemCount() = metas.size

    inner class MetaViewHolder(private val binding: ItemMetaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(meta: Meta) {
            binding.tvNombreMeta.text = meta.nombre
            binding.tvFechaMeta.text = "Meta: ${dateFormat.format(Date(meta.fechaObjetivo))}"

            val porcentaje = if (meta.montoObjetivo > 0) {
                ((meta.montoActual / meta.montoObjetivo) * 100).toInt()
            } else 0

            binding.progressMeta.progress = porcentaje.coerceIn(0, 100)
            binding.tvProgresoMeta.text = "$porcentaje%"

            binding.tvMontoMeta.text = "${currencyFormat.format(meta.montoActual)} " +
                    "de ${currencyFormat.format(meta.montoObjetivo)}"

            val faltante = meta.montoObjetivo - meta.montoActual
            binding.tvFaltante.text = "Falta: ${currencyFormat.format(faltante)}"

            // Calcular días restantes
            val diasRestantes = ((meta.fechaObjetivo - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
            val textoTiempo = if (diasRestantes > 0) {
                "~$diasRestantes días"
            } else if (diasRestantes == 0) {
                "Hoy"
            } else {
                "Vencida"
            }
            binding.tvTiempoRestante.text = textoTiempo
        }
    }
}