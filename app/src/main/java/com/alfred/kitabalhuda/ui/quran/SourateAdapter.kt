package com.alfred.kitabalhuda.ui.quran

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alfred.kitabalhuda.database.entity.SourateEntity
import com.alfred.kitabalhuda.databinding.ItemSourateBinding

class SourateAdapter(
    private val onClick: (SourateEntity) -> Unit,
    private val onOptionsClick: (SourateEntity) -> Unit,
    private val onLongClick: ((SourateEntity) -> Unit)? = null
) : RecyclerView.Adapter<SourateAdapter.SourateViewHolder>() {

    private var sourates: List<SourateEntity> = emptyList()
    private var currentlyPlayingSurahNumber: Int = -1
    private var availableSurahNumbers: Set<Int> = emptySet()

    fun submitList(list: List<SourateEntity>, availableSurahs: Set<Int> = emptySet()) {
        sourates = list
        availableSurahNumbers = availableSurahs
        notifyDataSetChanged()
    }

    fun setCurrentlyPlaying(numero: Int) {
        if (currentlyPlayingSurahNumber != numero) {
            currentlyPlayingSurahNumber = numero
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourateViewHolder {
        val binding = ItemSourateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SourateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SourateViewHolder, position: Int) {
        val sourate = sourates[position]
        holder.bind(sourate)
    }

    override fun getItemCount(): Int = sourates.size

    inner class SourateViewHolder(private val binding: ItemSourateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sourate: SourateEntity) {
            binding.textNumero.text = sourate.numero.toString()
            binding.textNomArabe.text = sourate.nomArabe
            binding.textNomPhonetique.text = sourate.nomPhonetique

            val context = binding.root.context
            val placeResId = if (sourate.lieuRevelation.equals("Mecca", ignoreCase = true) ||
                                 sourate.lieuRevelation.equals("Meccan", ignoreCase = true) ||
                                 sourate.lieuRevelation.equals("Mecquoise", ignoreCase = true)) {
                com.alfred.kitabalhuda.R.string.filter_mecca
            } else {
                com.alfred.kitabalhuda.R.string.filter_medina
            }

            val place = context.getString(placeResId)
            val ayahs = context.getString(com.alfred.kitabalhuda.R.string.ayahs_count)

            val isAvailable = availableSurahNumbers.isEmpty() || availableSurahNumbers.contains(sourate.numero)

            if (!isAvailable) {
                // Dim the card and show as unavailable
                binding.root.alpha = 0.4f
                binding.textInfo.text = context.getString(com.alfred.kitabalhuda.R.string.surah_unavailable)
                binding.audioBarsView.stopAnim()
                binding.root.setCardBackgroundColor(context.getColor(com.alfred.kitabalhuda.R.color.primary_dark))
            } else {
                // Render normally
                binding.root.alpha = 1.0f
                binding.textInfo.text = "$place • ${sourate.nombreVersets} $ayahs"

                // Show radial bars animation if currently playing
                if (sourate.numero == currentlyPlayingSurahNumber) {
                    binding.audioBarsView.startAnim()
                    binding.root.setCardBackgroundColor(context.getColor(com.alfred.kitabalhuda.R.color.gray_800))
                } else {
                    binding.audioBarsView.stopAnim()
                    binding.root.setCardBackgroundColor(context.getColor(com.alfred.kitabalhuda.R.color.primary_dark))
                }
            }

            binding.root.setOnClickListener { onClick(sourate) }
            binding.root.setOnLongClickListener {
                onLongClick?.invoke(sourate)
                true
            }
            binding.btnOptions.setOnClickListener { onOptionsClick(sourate) }
        }
    }
}
